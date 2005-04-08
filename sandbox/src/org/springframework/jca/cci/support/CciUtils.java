/*
 * Créé le 8 nov. 04
 *
 * Pour changer le modèle de ce fichier généré, allez à :
 * Fenêtre&gt;Préférences&gt;Java&gt;Génération de code&gt;Code et commentaires
 */
package org.springframework.jca.cci.support;

import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Generic utility methods for working with CCI.
 * 
 * @author Thierry TEMPLIER
 */
public abstract class CciUtils {
	private static final Log logger = LogFactory.getLog(CciUtils.class);

	/**
	 * Create an indexed record from the RecordFactory.
	 * @param connectionFactory connection factory to use
	 * @param name record name
	 * @return the record
	 */
	public static IndexedRecord createIndexedRecord(ConnectionFactory connectionFactory,String name) {
		try {
			RecordFactory factory=connectionFactory.getRecordFactory();
			return factory.createIndexedRecord(name);
		} catch(ResourceException ex) {
			throw new DataAccessResourceFailureException("Could not create a indexed record from the record factory");
		}
	}

	/**
	 * Create an mapped record from the RecordFactory.
	 * @param connectionFactory connection factory to use
	 * @param name record name
	 * @return the record
	 */
	public static MappedRecord createMappedRecord(ConnectionFactory connectionFactory,String name) {
		try {
			RecordFactory factory=connectionFactory.getRecordFactory();
			return factory.createMappedRecord(name);
		} catch(ResourceException ex) {
			throw new DataAccessResourceFailureException("Could not create a mapped record from the record factory");
		}
	}

	/**
	 * Close the given CCI resultset and ignore any thrown exception.
	 * This is useful for typical finally blocks in manual CCI code.
	 * @param interaction the CCI ResultSet to close
	 */
	public static void closeResultSet(ResultSet resultset) {
		if (resultset != null) {
			try {
				resultset.close();
			}
			catch (SQLException ex) {
				logger.warn("Could not close ResultSet", ex);
			}
		}
	}

	/**
	 * Close the given CCI interaction and ignore any thrown exception.
	 * This is useful for typical finally blocks in manual CCI code.
	 * @param interaction the CCI Interaction to close
	 */
	public static void closeInteration(Interaction interaction) {
		if (interaction != null) {
			try {
				interaction.close();
			}
			catch (ResourceException ex) {
				logger.warn("Could not close Interaction", ex);
			}
		}
	}
}
