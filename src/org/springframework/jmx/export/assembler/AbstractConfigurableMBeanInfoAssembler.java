package org.springframework.jmx.export.assembler;

import org.springframework.jmx.export.metadata.ManagedNotification;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.util.StringUtils;

import javax.management.JMException;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rob Harrop
 */
public abstract class AbstractConfigurableMBeanInfoAssembler extends AbstractReflectiveMBeanInfoAssembler {

	private ModelMBeanNotificationInfo[] notificationInfos;

	private Map notificationInfoMappings = new HashMap();

	public void setNotificationInfos(ManagedNotification[] notificationInfos) {
		ModelMBeanNotificationInfo[] infos = new ModelMBeanNotificationInfo[notificationInfos.length];
		for (int i = 0; i < notificationInfos.length; i++) {
			ManagedNotification notificationInfo = notificationInfos[i];
			infos[i] = JmxUtils.convertToModelMBeanNotificationInfo(notificationInfo);
		}
		this.notificationInfos = infos;
	}

	public void setNotificationInfoMappings(Map notificationInfoMappings) {
		Iterator entries = notificationInfoMappings.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			if (!(entry.getKey() instanceof String)) {
				throw new IllegalArgumentException("Property [notificationInfoMappings] only accepts Strings for Map keys.");
			}


			this.notificationInfoMappings.put(entry.getKey(), extractNotificationMetdata(entry.getValue()));
		}
	}


	protected ModelMBeanNotificationInfo[] getNotificationInfo(Object managedBean, String beanKey) throws JMException {
		ModelMBeanNotificationInfo[] result = null;

		if (StringUtils.hasText(beanKey)) {
			result = (ModelMBeanNotificationInfo[]) this.notificationInfoMappings.get(beanKey);
		}

		if (result == null) {
			result = this.notificationInfos;
		}

		return (result == null) ? new ModelMBeanNotificationInfo[0] : result;
	}

	private ModelMBeanNotificationInfo[] extractNotificationMetdata(Object mapValue) {
		if (mapValue instanceof ManagedNotification) {
			return new ModelMBeanNotificationInfo[]{JmxUtils.convertToModelMBeanNotificationInfo((ManagedNotification) mapValue)};
		}
		else if (mapValue instanceof Collection) {
			Collection col = (Collection) mapValue;
			List result = new ArrayList();
			for (Iterator iterator = col.iterator(); iterator.hasNext();) {
				Object colValue = iterator.next();
				if (!(colValue instanceof ManagedNotification)) {
					throw new IllegalArgumentException("Property [notificationInfoMappings] only accepts ManagedNotifications for Map values.");
				}
				result.add(JmxUtils.convertToModelMBeanNotificationInfo((ManagedNotification)colValue));
			}
			return (ModelMBeanNotificationInfo[]) result.toArray(new ModelMBeanNotificationInfo[result.size()]);
		}
		else {
			throw new IllegalArgumentException("Property [notificationInfoMappings] only accepts ManagedNotifications for Map values.");
		}
	}
}
