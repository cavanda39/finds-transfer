package fund.infrastructure.hibernate;

import java.sql.SQLException;
import java.util.EnumSet;

import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.extract.internal.DatabaseInformationImpl;
import org.hibernate.tool.schema.extract.spi.DatabaseInformation;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaMigrator;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.hibernate.tool.schema.spi.TargetDescriptor;

import com.google.common.collect.ImmutableList;

/**
 * @author a.polcaro
 *
 */
public final class HibernateSchema {

	private HibernateSchema() {
	}

	public static String generateUpdateScript() {
		final StringBuilder builder = new StringBuilder();
		generateUpdateScriptUsing(builder);
		return builder.toString();
	}

	public static boolean generateUpdateScriptUsing(StringBuilder builder){
		final int length = builder.length();
		final JdbcConnectionAccess jdbcConnectionAccess = HibernateInfoHolder.getServiceRegistry().getService(JdbcServices.class).getBootstrapJdbcConnectionAccess();
		final ConfigurationService cfgService = HibernateInfoHolder.getServiceRegistry().getService(ConfigurationService.class);
		final SchemaMigrator schemaMigrator = HibernateInfoHolder.getServiceRegistry().getService(SchemaManagementTool.class).getSchemaMigrator(cfgService.getSettings());

		final JdbcServices jdbcServices = HibernateInfoHolder.getServiceRegistry().getService(JdbcServices.class);
		final DatabaseInformation databaseInformation;
		try {
			HibernateInfoHolder.getServiceRegistry().getService(JdbcServices.class).getJdbcEnvironment();
			databaseInformation = new DatabaseInformationImpl(HibernateInfoHolder.getServiceRegistry(), HibernateInfoHolder.getServiceRegistry().getService(JdbcEnvironment.class),
					null, null, null);
//			databaseInformation = new DatabaseInformationImpl(
//					HibernateInfoHolder.getServiceRegistry(), 
//					HibernateInfoHolder.getServiceRegistry().getService(JdbcEnvironment.class), 
//					jdbcConnectionAccess, 
//					HibernateInfoHolder.getMetadata().getDatabase().getDefaultNamespace().getPhysicalName().getCatalog(),
//					HibernateInfoHolder.getMetadata().getDatabase().getDefaultNamespace().getPhysicalName().getSchema());
		} catch (SQLException e) {
			throw jdbcServices.getSqlExceptionHelper().convert(e, "Error creating DatabaseInformation for schema migration");
		}
		try {
//			schemaMigrator.doMigration(HibernateInfoHolder.getMetadata(), databaseInformation, true, new StringBuilderTarget(builder, ";"));
			return builder.length() > length;
		} finally {
			databaseInformation.cleanup();
		}
	}
	
	private static final class StringBuilderTarget implements TargetDescriptor {

		private final String delimiter;
		private final Formatter formatter;
		private final StringBuilder builder;

		public StringBuilderTarget(StringBuilder builder, String delimiter) {
			this(builder, delimiter, FormatStyle.NONE.getFormatter());
		}

		public StringBuilderTarget(StringBuilder builder, String delimiter, Formatter formatter) {
			this.formatter = formatter;
			this.delimiter = delimiter;
			this.builder = builder;
		}

		@Override
		public EnumSet<TargetType> getTargetTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ScriptTargetOutput getScriptTargetOutput() {
			// TODO Auto-generated method stub
			return null;
		}

	}
	

}
