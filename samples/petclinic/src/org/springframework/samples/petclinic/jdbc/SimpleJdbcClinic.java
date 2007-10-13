
package org.springframework.samples.petclinic.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.Specialty;
import org.springframework.samples.petclinic.Vet;
import org.springframework.samples.petclinic.Visit;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class for SimpleJDBC implementation of the Clinic interface.
 *
 * This class uses Java 5 features and the SimpleJdbcTemplate plus
 * SimpleJdbcInsert.  It also to take advantage of classes like 
 * BeanPropertySqlParameterSource and ParameterizedBeanPropertyRowMapper 
 * that provide automatic mapping between JabaBean properties and JDBC 
 * parameters or query reults.
 * 
 * This is a rewrite of the  AbstractJdbcClinic which was the base class 
 * for JDBC implementations of the Clinic interface for Spring 2.0.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 */
@Transactional
public class SimpleJdbcClinic implements Clinic, CachingClinic {

	private final Log logger = LogFactory.getLog(getClass());

	private SimpleJdbcTemplate simpleJdbcTemplate;
	
	private SimpleJdbcInsert insertOwner;
	private SimpleJdbcInsert insertPet;
	private SimpleJdbcInsert insertVisit;
	
	private final List<Vet> vets = new ArrayList<Vet>();

	@Resource(name = "dataSource")
	public void init(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
		
		this.insertOwner = new SimpleJdbcInsert(dataSource)
			.withTableName("owners")
			.usingGeneratedKeyColumns("id");
		this.insertPet = new SimpleJdbcInsert(dataSource)
			.withTableName("pets")
			.usingGeneratedKeyColumns("id");
		this.insertVisit = new SimpleJdbcInsert(dataSource)
			.withTableName("visits")
			.usingGeneratedKeyColumns("id");
	}	


	@Transactional(readOnly = true)
	public void refreshVetsCache() throws DataAccessException {
		synchronized (this.vets) {
			this.logger.info("Refreshing vets cache");

			// Retrieve the list of all vets.
			this.vets.clear();
			this.vets.addAll(this.simpleJdbcTemplate.query(
					"SELECT id, first_name, last_name FROM vets ORDER BY last_name,first_name", 
					ParameterizedBeanPropertyRowMapper.newInstance(Vet.class)));

			// Retrieve the list of all possible specialties.
			final List<Specialty> specialties = this.simpleJdbcTemplate.query(
					"SELECT id, name FROM specialties", 
					ParameterizedBeanPropertyRowMapper.newInstance(Specialty.class));

			// Build each vet's list of specialties.
			for (Vet vet : this.vets) {
				final List<Integer> vetSpecialtiesIds = this.simpleJdbcTemplate.query(
						"SELECT specialty_id FROM vet_specialties WHERE vet_id=?", 
						new ParameterizedRowMapper<Integer>() {
							public Integer mapRow(ResultSet rs, int row) throws SQLException {
								return Integer.valueOf(rs.getInt(1));
							}},
						vet.getId().intValue());
				for (int specialtyId : vetSpecialtiesIds) {
					Specialty specialty = EntityUtils.getById(specialties, Specialty.class, specialtyId);
					vet.addSpecialty(specialty);
				}
			}
		}
	}

	// START of Clinic implementation section *******************************

	@Transactional(readOnly = true)
	public Collection<Vet> getVets() throws DataAccessException {
		synchronized (this.vets) {
			if (this.vets.isEmpty()) {
				refreshVetsCache();
			}
			return this.vets;
		}
	}

	@Transactional(readOnly = true)
	public Collection<PetType> getPetTypes() throws DataAccessException {
		return this.simpleJdbcTemplate.query(
				"SELECT id, name FROM types ORDER BY name", 
				ParameterizedBeanPropertyRowMapper.newInstance(PetType.class));
	}

	/** Method loads owners plus pets and visits if not already loaded */
	@Transactional(readOnly = true)
	public Collection<Owner> findOwners(String lastName) throws DataAccessException {
		List<Owner> owners = this.simpleJdbcTemplate.query(
				"SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name like ?",
				ParameterizedBeanPropertyRowMapper.newInstance(Owner.class),
				lastName + "%");
		loadOwnersPetsAndVisits(owners);
		return owners;
	}

	/** Method loads an owner plus pets and visits if not already loaded */
	@Transactional(readOnly = true)
	public Owner loadOwner(int id) throws DataAccessException {
		Owner owner;
		try {
			owner = this.simpleJdbcTemplate.queryForObject(
					"SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE id=?",
					ParameterizedBeanPropertyRowMapper.newInstance(Owner.class),
					id);
		} catch (EmptyResultDataAccessException e) {
			throw new ObjectRetrievalFailureException(Owner.class, new Integer(id));
		}
		loadPetsAndVisits(owner);
		return owner;
	}

	@Transactional(readOnly = true)
	public Pet loadPet(int id) throws DataAccessException {
		JdbcPet pet;
		try {
			pet = this.simpleJdbcTemplate.queryForObject(
					"SELECT id, name, birth_date, type_id, owner_id FROM pets WHERE id=?",
					new JdbcPetRowMapper(),
					id);
		} catch (EmptyResultDataAccessException e) {
			throw new ObjectRetrievalFailureException(Pet.class, new Integer(id));
		}
		Owner owner = loadOwner(pet.getOwnerId());
		owner.addPet(pet);
		loadVisits(pet);
		return pet;
	}

	public void storeOwner(Owner owner) throws DataAccessException {
		if (owner.isNew()) {
			Number newKey = this.insertOwner.executeAndReturnKey(
					new BeanPropertySqlParameterSource(owner));
			owner.setId(newKey.intValue());
		}
		else {
			this.simpleJdbcTemplate.update(
					"UPDATE owners SET first_name=:firstName, last_name=:lastName, address=:address, " +
					"city=:city, telephone=:telephone WHERE id=:id",
					new BeanPropertySqlParameterSource(owner));
		}
	}

	public void storePet(Pet pet) throws DataAccessException {
		if (pet.isNew()) {
			Number newKey = this.insertPet.executeAndReturnKey(
					createPetParameterSource(pet));
			pet.setId(newKey.intValue());
		}
		else {
			this.simpleJdbcTemplate.update(
					"UPDATE pets SET name=:name, birth_date=:birth_date, type_id=:type_id, " +
					"owner_id=:owner_id WHERE id=:id",
					createPetParameterSource(pet));
		}
	}

	public void storeVisit(Visit visit) throws DataAccessException {
		if (visit.isNew()) {
			Number newKey = this.insertVisit.executeAndReturnKey(
					createVisitParameterSource(visit));
			visit.setId(newKey.intValue());
		}
		else {
			throw new UnsupportedOperationException("Visit update not supported");
		}
	}

	// END of Clinic implementation section *******************************

	/**
	 * Method to create a MapSqlParameterSource based on data values
	 * from a Pet instance.
	 */
	private MapSqlParameterSource createPetParameterSource(Pet pet) {
		return new MapSqlParameterSource()
			.addValue("id", pet.getId())
			.addValue("name", pet.getName())
			.addValue("birth_date", pet.getBirthDate())
			.addValue("type_id", pet.getType().getId())
			.addValue("owner_id", pet.getOwner().getId());
	}

	/**
	 * Method to create a MapSqlParameterSource based on data values
	 * from a Visit instance.
	 */
	private MapSqlParameterSource createVisitParameterSource(Visit visit) {
		return new MapSqlParameterSource()
			.addValue("id", visit.getId())
			.addValue("visit_date", visit.getDate())
			.addValue("description", visit.getDescription())
			.addValue("pet_id", visit.getPet().getId());
	}

	/**
	 * Method to retrieve the <code>Visit</code> data for a <code>Pet</code>.
	 */
	private void loadVisits(JdbcPet pet) {
		pet.setType(EntityUtils.getById(getPetTypes(), PetType.class, pet.getTypeId()));
		final List<Visit> visits = this.simpleJdbcTemplate.query(
				"SELECT id, visit_date, description FROM visits WHERE pet_id=?", 
				new ParameterizedRowMapper<Visit>() {
					public Visit mapRow(ResultSet rs, int row)
							throws SQLException {
						Visit visit = new Visit();
						visit.setId(rs.getInt("id"));
						visit.setDate(rs.getTimestamp("visit_date"));
						visit.setDescription(rs.getString("description"));
						return visit;
					}
					
				}, 
				pet.getId().intValue()); 
		for (Visit visit : visits) {
			pet.addVisit(visit);
		}
	}

	/**
	 * Method to retrieve the <code>Pet</code> and <code>Visit</code> data
	 * for an <code>Owner</code>.
	 */
	private void loadPetsAndVisits(final Owner owner) {
		final List<JdbcPet> pets = this.simpleJdbcTemplate.query(
				"SELECT id, name, birth_date, type_id, owner_id FROM pets WHERE owner_id=?",
				new JdbcPetRowMapper(),
				owner.getId().intValue()); 
		for (JdbcPet pet : pets) {
			owner.addPet(pet);
			loadVisits(pet);
		}
	}

	/**
	 * Method to retrieve a <code>List</code> of <code>Owner</code>s and
	 * their <code>Pet</code> and <code>Visit</code> data.
	 *
	 * @param owners <code>List</code>.
	 * @see #loadPetsAndVisits(Owner)
	 */
	private void loadOwnersPetsAndVisits(List<Owner> owners) {
		for (Owner owner : owners) {
			loadPetsAndVisits(owner);
		}
	}

	/**
	 * ParameterizedRowMapper implementation mapping data from the ResultSet to the
	 * corresponding properties of the JdbcPet class. 
	 */
	private class JdbcPetRowMapper implements ParameterizedRowMapper<JdbcPet> {

		public JdbcPet mapRow(ResultSet rs, int rownum) throws SQLException {
			JdbcPet pet = new JdbcPet();
			pet.setId(rs.getInt("id"));
			pet.setName(rs.getString("name"));
			pet.setBirthDate(rs.getDate("birth_date"));
			pet.setTypeId(rs.getInt("type_id"));
			pet.setOwnerId(rs.getInt("owner_id"));
			return pet;
		}
		
	}
	
}
