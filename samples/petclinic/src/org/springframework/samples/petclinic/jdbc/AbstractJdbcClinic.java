package org.springframework.samples.petclinic.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Entity;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.Specialty;
import org.springframework.samples.petclinic.Vet;
import org.springframework.samples.petclinic.Visit;
import org.springframework.samples.petclinic.util.EntityUtils;

/**
 * Base class for JDBC implementations of the Clinic interface.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
abstract public class AbstractJdbcClinic extends JdbcDaoSupport implements Clinic {

	/** Logger for this class and subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Holds all vets Query Object. */
	private VetsQuery vetsQuery;

	/** Holds specialties Query Object. */
	private SpecialtiesQuery specialtiesQuery;

	/** Holds vet specialties Query Object. */
	private VetSpecialtiesQuery vetSpecialtiesQuery;

	private OwnersByNameQuery ownersByNameQuery;

	private OwnerQuery ownerQuery;

	private OwnerInsert ownerInsert;

	private OwnerUpdate ownerUpdate;

	private PetsByOwnerQuery petsByOwnerQuery;

	private PetQuery petQuery;

	private PetInsert petInsert;

	private PetUpdate petUpdate;

	private PetTypesQuery petTypesQuery;

	private VisitsQuery visitsQuery;

	private VisitInsert visitInsert;


	protected void initDao() {
		this.vetsQuery = new VetsQuery(getDataSource());
		this.specialtiesQuery = new SpecialtiesQuery(getDataSource());
		this.vetSpecialtiesQuery = new VetSpecialtiesQuery(getDataSource());
		this.petTypesQuery = new PetTypesQuery(getDataSource());
		this.ownersByNameQuery = new OwnersByNameQuery(getDataSource());
		this.ownerQuery = new OwnerQuery(getDataSource());
		this.ownerInsert = new OwnerInsert(getDataSource());
		this.ownerUpdate = new OwnerUpdate(getDataSource());
		this.petsByOwnerQuery = new PetsByOwnerQuery(getDataSource());
		this.petQuery = new PetQuery(getDataSource());
		this.petInsert = new PetInsert(getDataSource());
		this.petUpdate = new PetUpdate(getDataSource());
		this.visitsQuery = new VisitsQuery(getDataSource());
		this.visitInsert = new VisitInsert(getDataSource());
	}


	// START of Clinic implementation section *******************************

	public Collection getVets() throws DataAccessException {
		// establish the Map of all vets
		List vets = this.vetsQuery.execute();

		// establish the map of all the possible specialties
		List specialties = this.specialtiesQuery.execute();

		// establish each vet's List of specialties
		Iterator vi = vets.iterator();
		while (vi.hasNext()) {
			Vet vet = (Vet) vi.next();
			List vetSpecialtiesIds = this.vetSpecialtiesQuery.execute(vet.getId());
			Iterator vsi = vetSpecialtiesIds.iterator();
			while (vsi.hasNext()) {
				int specialtyId = ((Integer) vsi.next()).intValue();
				Specialty specialty = (Specialty) EntityUtils.getById(specialties, Specialty.class, specialtyId);
				vet.addSpecialty(specialty);
			}
		}

		return vets;
	}

	public Collection getPetTypes() throws DataAccessException {
		return this.petTypesQuery.execute();
	}

	/** Method loads owners plus pets and visits if not already loaded */
	public Collection findOwners(String lastName) throws DataAccessException {
		List owners = this.ownersByNameQuery.execute(lastName + "%");
		loadOwnersPetsAndVisits(owners);
		return owners;
	}

	/** Method loads an owner plus pets and visits if not already loaded */
	public Owner loadOwner(int id)  throws DataAccessException {
		Owner owner = (Owner) this.ownerQuery.findObject(id);
		if (owner == null) {
			throw new ObjectRetrievalFailureException(Owner.class, new Integer(id));
		}
		loadPetsAndVisits(owner);
		return owner;
	}

	public Pet loadPet(int id) throws DataAccessException {
		JdbcPet pet = (JdbcPet) this.petQuery.findObject(id);
		if (pet == null) {
			throw new ObjectRetrievalFailureException(Pet.class, new Integer(id));
		}
		Owner owner = loadOwner(pet.getOwnerId());
		owner.addPet(pet);
		loadVisits(pet);
		return pet;
	}

	public void storeOwner(Owner owner) throws DataAccessException {
		if (owner.isNew()) {
			this.ownerInsert.insert(owner);
		}
		else {
			this.ownerUpdate.update(owner);
		}
	}

	public void storePet(Pet pet) throws DataAccessException {
		if (pet.isNew()) {
			this.petInsert.insert(pet);
		}
		else {
			this.petUpdate.update(pet);
		}
	}

	public void storeVisit(Visit visit) throws DataAccessException {
		if (visit.isNew()) {
			this.visitInsert.insert(visit);
		}
		else {
			throw new UnsupportedOperationException("Visit update not supported");
		}
	}

	// END of Clinic implementation section *******************************


	/**
	 * Method maps a List of Entity objects keyed to their ids.
	 * @param list List containing Entity objects
	 * @return Map containing Entity objects
	 */
	protected final Map mapEntityList(List list) {
		Map map = new HashMap();
		Iterator iterator = list.iterator();
		while (iterator.hasNext()) {
			Entity entity = (Entity) iterator.next();
			map.put(new Integer(entity.getId()), entity);
		}
		return map;
	}

	/**
	 * Method to retrieve the <code>Visit</code> data for a <code>Pet</code>.
	 */
	protected void loadVisits(JdbcPet pet) {
		pet.setType((PetType) EntityUtils.getById(getPetTypes(), PetType.class, pet.getTypeId()));
		List visits = this.visitsQuery.execute(pet.getId());
		Iterator vi = visits.iterator();
		while (vi.hasNext()) {
			Visit visit = (Visit) vi.next();
			pet.addVisit(visit);
		}
	}

	/**
	 * Method to retrieve the <code>Pet</code> and <code>Visit</code>
	 * data for an <code>Owner</code>.
	 */
	protected void loadPetsAndVisits(Owner owner) {
		List pets = this.petsByOwnerQuery.execute(owner.getId());
		Iterator pi = pets.iterator();
		while (pi.hasNext()) {
			JdbcPet pet = (JdbcPet) pi.next();
			owner.addPet(pet);
			loadVisits(pet);
		}
	}

	/**
	 * Method to retrieve a <code>List</code> of <code>Owner</code>s
	 * and their <code>Pet</code> and <code>Visit</code> data.
	 * @param owners <code>List</code>.
	 * @see #loadPetsAndVisits(Owner)
	 */
	protected void loadOwnersPetsAndVisits(List owners) {
		Iterator oi = owners.iterator();
		while (oi.hasNext()) {
			Owner owner = (Owner) oi.next();
			loadPetsAndVisits(owner);
		}
	}

	protected void retrieveIdentity(final Entity entity) {
		entity.setId(getJdbcTemplate().queryForInt(getIdentityQuery()));
	}

	/**
	 * Return the identity query for the particular database:
	 * a query that can be used to retrieve the id of a row
	 * that has just been inserted.
	 * @return the identity query
	 */
	protected abstract String getIdentityQuery();


	// ************* Operation Objects section ***************

	/**
	 * Base class for all <code>Vet</code> Query Objects.
	 */
	protected class VetsQuery extends MappingSqlQuery {

		/**
		 * Create a new instance of VetsQuery.
		 * @param ds the DataSource to use for the query
		 * @param sql SQL string to use for the query
		 */
		protected VetsQuery(DataSource ds, String sql) {
			super(ds, sql);
		}

		/**
		 * Create a new instance of VetsQuery that returns all vets.
		 * @param ds the DataSource to use for the query
		 */
		protected VetsQuery(DataSource ds) {
			super(ds, "SELECT id,first_name,last_name FROM vets ORDER BY last_name,first_name");
			compile();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			Vet vet = new Vet();
			vet.setId(rs.getInt("id"));
			vet.setFirstName(rs.getString("first_name"));
			vet.setLastName(rs.getString("last_name"));
			return vet;
		}
	}


	/**
	 * All <code>Vet</code>s specialties Query Object.
	 */
	protected class SpecialtiesQuery extends MappingSqlQuery {

		/**
		 * Create a new instance of SpecialtiesQuery.
		 * @param ds the DataSource to use for the query
		 */
		protected SpecialtiesQuery(DataSource ds) {
			super(ds, "SELECT id,name FROM specialties");
			compile();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			Specialty specialty = new Specialty();
			specialty.setId(rs.getInt("id"));
			specialty.setName(rs.getString("name"));
			return specialty;
		}
	}


	/**
	 * A particular <code>Vet</code>'s specialties Query Object.
	 */
	protected class VetSpecialtiesQuery extends MappingSqlQuery {

		/**
		 * Create a new instance of VetSpecialtiesQuery.
		 * @param ds the DataSource to use for the query
		 */
		protected VetSpecialtiesQuery(DataSource ds) {
			super(ds, "SELECT specialty_id FROM vet_specialties WHERE vet_id=?");
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			return new Integer(rs.getInt("specialty_id"));
		}
	}


	/**
	 * Abstract base class for all <code>Owner</code> Query Objects.
	 */
	protected abstract class OwnersQuery extends MappingSqlQuery {

		/**
		 * Create a new instance of OwnersQuery.
		 * @param ds the DataSource to use for the query
		 * @param sql SQL string to use for the query
		 */
		protected OwnersQuery(DataSource ds, String sql) {
			super(ds, sql);
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			Owner owner = new Owner();
			owner.setId(rs.getInt("id"));
			owner.setFirstName(rs.getString("first_name"));
			owner.setLastName(rs.getString("last_name"));
			owner.setAddress(rs.getString("address"));
			owner.setCity(rs.getString("city"));
			owner.setTelephone(rs.getString("telephone"));
			return owner;
		}
	}


	/**
	 * <code>Owner</code>s by last name Query Object.
	 */
	protected class OwnersByNameQuery extends OwnersQuery {

		/**
		 * Create a new instance of OwnersByNameQuery.
		 * @param ds the DataSource to use for the query
		 */
		protected OwnersByNameQuery(DataSource ds) {
			super(ds, "SELECT id,first_name,last_name,address,city,telephone FROM owners WHERE last_name like ?");
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}
	}


	/**
	 * <code>Owner</code> by id Query Object.
	 */
	protected class OwnerQuery extends OwnersQuery {

		/**
		 * Create a new instance of OwnerQuery.
		 * @param ds the DataSource to use for the query
		 */
		protected OwnerQuery(DataSource ds) {
			super(ds, "SELECT id,first_name,last_name,address,city,telephone FROM owners WHERE id=?");
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}
	}


	/**
	 * <code>Owner</code> Insert Object.
	 */
	protected class OwnerInsert extends SqlUpdate {

		/**
		 * Create a new instance of OwnerInsert.
		 * @param ds the DataSource to use for the insert
		 */
		protected OwnerInsert(DataSource ds) {
			super(ds, "INSERT INTO owners VALUES(?,?,?,?,?,?)");
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		protected void insert(Owner owner) {
			Object[] objs = new Object[] {null, owner.getFirstName(), owner.getLastName(),
																		owner.getAddress(), owner.getCity(), owner.getTelephone()};
			super.update(objs);
			retrieveIdentity(owner);
		}
	}


	/**
	 * <code>Owner</code> Update Object.
	 */
	protected class OwnerUpdate extends SqlUpdate {

		/**
		 * Create a new instance of OwnerUpdate.
		 * @param ds the DataSource to use for the update
		 */
		protected OwnerUpdate(DataSource ds) {
			super(ds, "UPDATE owners SET first_name=?,last_name=?,address=?,city=?,telephone=? WHERE id=?");
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		/**
		 * Method to update an <code>Owner</code>'s data.
		 * @param owner to update
		 * @return the number of rows affected by the update
		 */
		protected int update(Owner owner) {
			return this.update(new Object[] {owner.getFirstName(), owner.getLastName(), owner.getAddress(),
																			 owner.getCity(), owner.getTelephone(), new Integer(owner.getId())});
		}
	}


	/**
	 * Abstract base class for all <code>Pet</code> Query Objects.
	 */
	protected abstract class PetsQuery extends MappingSqlQuery {

		/**
		 * Create a new instance of PetsQuery.
		 * @param ds the DataSource to use for the query
		 * @param sql SQL string to use for the query
		 */
		protected PetsQuery(DataSource ds, String sql) {
			super(ds, sql);
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			JdbcPet pet = new JdbcPet();
			pet.setId(rs.getInt("id"));
			pet.setName(rs.getString("name"));
			pet.setBirthDate(rs.getDate("birth_date"));
			pet.setTypeId(rs.getInt("type_id"));
			pet.setOwnerId(rs.getInt("owner_id"));
			return pet;
		}
	}


	/**
	 * <code>Pet</code>s by <code>Owner</code> Query Object.
	 */
	protected class PetsByOwnerQuery extends PetsQuery {

		/**
		 * Create a new instance of PetsByOwnerQuery.
		 * @param ds the DataSource to use for the query
		 */
		protected PetsByOwnerQuery(DataSource ds) {
			super(ds, "SELECT id,name,birth_date,type_id,owner_id FROM pets WHERE owner_id=?");
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}
	}


	/**
	 * <code>Pet</code> by id Query Object.
	 */
    protected class PetQuery extends PetsQuery {

		/**
		 * Create a new instance of PetQuery.
		 * @param ds the DataSource to use for the query
		 */
		protected PetQuery(DataSource ds) {
			super(ds, "SELECT id,name,birth_date,type_id,owner_id FROM pets WHERE id=?");
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}
	}


	/**
	 * <code>Pet</code> Insert Object.
	 */
	protected class PetInsert extends SqlUpdate {

		/**
		 * Create a new instance of PetInsert.
		 * @param ds the DataSource to use for the insert
		 */
		protected PetInsert(DataSource ds) {
			super(ds, "INSERT INTO pets VALUES(?,?,?,?,?)");
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.DATE));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		/**
		 * Method to insert a new <code>Pet</code>.
		 * @param pet to insert
		 */
		protected void insert(Pet pet) {
			Object[] objs = new Object[] {null, pet.getName(), new java.sql.Date(pet.getBirthDate().getTime()),
																		new Integer(pet.getType().getId()), new Integer(pet.getOwner().getId())};
			super.update(objs);
			retrieveIdentity(pet);
		}
	}


	/**
	 * <code>Pet</code> Update Object.
	 */
	protected class PetUpdate extends SqlUpdate {

		/**
		 * Create a new instance of PetUpdate.
		 * @param ds the DataSource to use for the update
		 */
		protected PetUpdate(DataSource ds) {
			super(ds, "UPDATE pets SET name=?,birth_date=?,type_id=?,owner_id=? WHERE id=?");
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.DATE));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		/**
		 * Method to update an <code>Pet</code>'s data.
		 * @param pet to update
		 * @return the number of rows affected by the update
		 */
		protected int update(Pet pet) {
			return this.update(new Object[] {pet.getName(), new java.sql.Date(pet.getBirthDate().getTime()),
																			 new Integer(pet.getType().getId()), new Integer(pet.getOwner().getId()),
																			 new Integer(pet.getId())});
		}
	}


	/**
	 * All <code>Pet</code> types Query Object.
	 */
	protected class PetTypesQuery extends MappingSqlQuery {

		/**
		 * Create a new instance of PetTypesQuery.
		 * @param ds the DataSource to use for the query
		 */
		protected PetTypesQuery(DataSource ds) {
			super(ds, "SELECT id,name FROM types ORDER BY name");
			compile();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			PetType type = new PetType();
			type.setId(rs.getInt("id"));
			type.setName(rs.getString("name"));
			return type;
		}
	}


	/**
	 * <code>Visit</code>s by <code>Pet</code> Query Object.
	 */
	protected class VisitsQuery extends MappingSqlQuery {

		/**
		 * Create a new instance of VisitsQuery.
		 * @param ds the DataSource to use for the query
		 */
		protected VisitsQuery(DataSource ds) {
			super(ds, "SELECT id,visit_date,description FROM visits WHERE pet_id=?");
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			Visit visit = new Visit();
			visit.setId(rs.getInt("id"));
			visit.setDate(rs.getDate("visit_date"));
			visit.setDescription(rs.getString("description"));
			return visit;
		}
	}


	/**
	 * <code>Visit</code> Insert Object.
	 */
	protected class VisitInsert extends SqlUpdate {

		/**
		 * Create a new instance of VisitInsert.
		 * @param ds the DataSource to use for the insert
		 */
		protected VisitInsert(DataSource ds) {
			super(ds, "INSERT INTO visits VALUES(?,?,?,?)");
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.DATE));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		/**
		 * Method to insert a new <code>Visit</code>.
		 * @param visit to insert
		 */
		protected void insert(Visit visit) {
			Object[] objs =
					new Object[]{
						null,
						new Integer(visit.getPet().getId()),
						new java.sql.Date(visit.getDate().getTime()),
						visit.getDescription()
					};
			super.update(objs);
			retrieveIdentity(visit);
		}
	}

}
