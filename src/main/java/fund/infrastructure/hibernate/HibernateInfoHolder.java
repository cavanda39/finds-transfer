package fund.infrastructure.hibernate;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

final class HibernateInfoHolder {

	private HibernateInfoHolder() {
	}

	private static Metadata metadata;
	private static SessionFactoryServiceRegistry serviceRegistry;

	static void setMetadata(Metadata metadata) {
		HibernateInfoHolder.metadata = metadata;
	}

	static void setServiceRegistry(SessionFactoryServiceRegistry serviceRegistry) {
		HibernateInfoHolder.serviceRegistry = serviceRegistry;
	}

	public static MetadataImplementor getMetadata() {
		return (MetadataImplementor) metadata;
	}

	public static SessionFactoryServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}
	

}
