package ecommerce.service.order.dao;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
//
//import com.datastax.driver.core.AuthProvider;
//import com.datastax.driver.core.PlainTextAuthProvider;
//
////@Configuration
//public class EcommerceOrderCassandraConfiguration extends AbstractCassandraConfiguration {
//
//	@Value("${spring.data.cassandra.keyspace-name}")
//	private String keyspaceName;
//   
//	@Value("${spring.data.cassandra.contact-points}")
//	private String contactPoints;
//
//	@Value("${spring.data.cassandra.port}")
//	private int port;
//
//	@Value("${spring.data.cassandra.cluster-name}")
//	private String clusterName;
//
//	@Value("${spring.data.cassandra.username}")
//	private String username;
//
//	@Value("${spring.data.cassandra.password}")
//	private String password;
//
//	@Override
//	protected String getKeyspaceName() {
//		return keyspaceName;
//	}
//
//	@Override
//	public String getContactPoints() {
//		return contactPoints;
//	}
//
//	@Override
//	public int getPort() {
//		return this.port;
//	}
//
//	@Override
//	public String getClusterName() {
//		return clusterName;
//	}
//
//	@Override
//	protected boolean getMetricsEnabled() {
//		return false;
//	}
//
//	@Override
//	protected AuthProvider getAuthProvider() {
//		return new PlainTextAuthProvider(username,password);
//	}
//
//	@Override
//	public String[] getEntityBasePackages() {
//		return new String[]{EcommerceOrder.class.getName()};
//	}
//
//}
