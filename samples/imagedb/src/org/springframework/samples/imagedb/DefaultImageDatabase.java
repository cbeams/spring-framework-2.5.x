package org.springframework.samples.imagedb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.FileCopyUtils;

/**
 * Default implementation of the central business interface.
 * Uses JDBC with a LobHandler to retrieve and store image data.
 * @author Juergen Hoeller
 * @since 07.01.2004
 */
public class DefaultImageDatabase extends JdbcDaoSupport implements ImageDatabase {

	private LobHandler lobHandler;

	private GetImagesQuery getImagesQuery;

	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	protected void initDao() throws Exception {
		this.getImagesQuery = new GetImagesQuery(getDataSource());
	}


	public List getImages() throws DataAccessException {
		return this.getImagesQuery.execute();
	}

	public void streamImage(String name, OutputStream os) throws DataAccessException, IOException {
		String sql = "SELECT content FROM imagedb WHERE image_name=?";
		Connection con = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, name);
			rs = ps.executeQuery();
			if (!rs.next()) {
				throw new DataRetrievalFailureException("Image with name '" + name + "' not found in database");
			}
			FileCopyUtils.copy(this.lobHandler.getBlobAsBinaryStream(rs, 1), os);
		}
		catch (SQLException ex) {
			getJdbcTemplate().getExceptionTranslator().translate("streamImage", sql, ex);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
			closeConnectionIfNecessary(con);
		}
	}

	public void storeImage(String name, InputStream is, String description) throws IOException {
		String sql = "INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)";
		Connection con = getConnection();
		PreparedStatement ps = null;
		LobCreator lobCreator = this.lobHandler.getLobCreator();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, name);
			lobCreator.setBlobAsBinaryStream(ps, 2, is, is.available());
			lobCreator.setClobAsString(ps, 3, description);
			ps.executeUpdate();
		}
		catch (SQLException ex) {
			getExceptionTranslator().translate("streamImage", sql, ex);
		}
		finally {
			lobCreator.close();
			JdbcUtils.closeStatement(ps);
			closeConnectionIfNecessary(con);
		}
	}

	public void checkImages() {
		// could implement consistency check here
		logger.info("Checking images: not implemented but invoked by scheduling");
	}

	public void clearDatabase() {
		getJdbcTemplate().update("DELETE FROM imagedb");
	}


	protected class GetImagesQuery extends MappingSqlQuery {

		public GetImagesQuery(DataSource ds) {
			super(ds, "SELECT image_name, description FROM imagedb");
			compile();
		}

		protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			String name = rs.getString(1);
			String description = lobHandler.getClobAsString(rs, 2);
			return new ImageDescriptor(name, description);
		}
	}

}
