package org.bremersee.data.ldaptive;

import java.time.Duration;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.ConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PruneStrategy;
import org.ldaptive.pool.SearchValidator;

/**
 * The ldaptive connection pool factory.
 */
public interface LdaptiveConnectionPoolFactory {

  /**
   * Creates connection pool.
   *
   * @param properties the properties
   * @param defaultConnectionFactory the default connection factory
   * @return the connection pool
   */
  ConnectionPool createConnectionPool(
      LdaptiveProperties properties,
      DefaultConnectionFactory defaultConnectionFactory);

  /**
   * Default connection pool factory.
   *
   * @return the ldaptive connection pool factory
   */
  static LdaptiveConnectionPoolFactory defaultFactory() {
    return new Default();
  }

  /**
   * The default ldaptive connection pool factory.
   */
  class Default implements LdaptiveConnectionPoolFactory {

    @Override
    public ConnectionPool createConnectionPool(
        LdaptiveProperties properties,
        DefaultConnectionFactory defaultConnectionFactory) {
      BlockingConnectionPool pool = new BlockingConnectionPool();
      pool.setConnectionFactory(defaultConnectionFactory);
      pool.setPoolConfig(poolConfig(properties));
      pool.setPruneStrategy(pruneStrategy(properties));
      pool.setValidator(searchValidator(properties));
      if (properties.getBlockWaitTime() > 0L) {
        pool.setBlockWaitTime(Duration.ofMillis(properties.getBlockWaitTime()));
      }
      return pool;
    }

    private PoolConfig poolConfig(LdaptiveProperties properties) {
      PoolConfig pc = new PoolConfig();
      pc.setMaxPoolSize(properties.getMaxPoolSize());
      pc.setMinPoolSize(properties.getMinPoolSize());
      pc.setValidateOnCheckIn(properties.isValidateOnCheckIn());
      pc.setValidateOnCheckOut(properties.isValidateOnCheckOut());
      if (properties.getValidatePeriod() > 0L) {
        pc.setValidatePeriod(Duration.ofSeconds(properties.getValidatePeriod()));
      }
      pc.setValidatePeriodically(properties.isValidatePeriodically());
      return pc;
    }

    private PruneStrategy pruneStrategy(LdaptiveProperties properties) {
      // there may be other ways
      IdlePruneStrategy ips = new IdlePruneStrategy();
      if (properties.getIdleTime() > 0L) {
        ips.setIdleTime(Duration.ofSeconds(properties.getIdleTime()));
      }
      if (properties.getPrunePeriod() > 0L) {
        ips.setPrunePeriod(Duration.ofSeconds(properties.getPrunePeriod()));
      }
      return ips;
    }

    private SearchValidator searchValidator(LdaptiveProperties properties) {
      return properties.getSearchValidator();
    }

  }

}
