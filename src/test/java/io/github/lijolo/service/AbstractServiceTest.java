package io.github.lijolo.service;

import io.github.lijolo.model.helper.LiJoLoLiquiBaseTestContainer;
import io.github.lijolo.service.helper.LiJoLoFileUtils;
import io.github.lijolo.service.helper.LiJoLoPostgresqlContainer;
import org.apache.commons.io.FileUtils;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.net.JarURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public abstract class AbstractServiceTest {

  private static LiJoLoPostgresqlContainer database;

  public AbstractServiceTest() {
  }

  private static void startDatabase(String databaseName,
                                    Path liquibaseDir,
                                    Path liquibaseDirForLoadingUnitTestData) {
    try {
      AbstractServiceTest.database = new LiJoLoPostgresqlContainer().withUsername("test")
                                                                    .withPassword("test")
                                                                    .withDatabaseName(databaseName);
      database.start();
      database.waitingFor(Wait.forHealthcheck());

      Connection connection01 = DriverManager.getConnection(database.getJdbcUrl(),
                                                            "test",
                                                            "test");
      LiJoLoLiquiBaseTestContainer.initFunction(connection01,
                                                liquibaseDir,
                                                "init/db.changelog-init.yaml");
      connection01.close();

      Connection connection02 = DriverManager.getConnection(database.getJdbcUrl(),
                                                            "test",
                                                            "test");
      LiJoLoLiquiBaseTestContainer.initFunction(connection02,
                                                liquibaseDirForLoadingUnitTestData,
                                                "db.changelog-root-unit-test.yaml");
      connection02.close();
    } catch (SQLException e) {
      System.out.println("SQLException during start of database -> " + e.getMessage());
    }
  }

  private Path copyLiquibaseInitChangelogToTempDir() {
    try {
      Path tempDirectory = Files.createTempDirectory(Path.of(String.valueOf(Paths.get(System.getProperty("java.io.tmpdir")))),
                                                     "test");
      JarURLConnection changelog = (JarURLConnection) Objects.requireNonNull(getClass().getClassLoader()
                                                                                       .getResource("db/changelog"))
                                                             .toURI()
                                                             .toURL()
                                                             .openConnection();
      LiJoLoFileUtils.copyJarResourcesRecursively(tempDirectory.toFile(),
                                                  changelog);
      return tempDirectory;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Path copyLiquibaseForLoadingUnitTestDataChangelogToTempDir() {
    try {
      Path tempDirectory = Files.createTempDirectory(Path.of(String.valueOf(Paths.get(System.getProperty("java.io.tmpdir")))),
                                                     "test");
      JarURLConnection changelog = (JarURLConnection) Objects.requireNonNull(getClass().getClassLoader()
                                                                                       .getResource("db/changelog-unit-test"))
                                                             .toURI()
                                                             .toURL()
                                                             .openConnection();
      LiJoLoFileUtils.copyJarResourcesRecursively(tempDirectory.toFile(),
                                                  changelog);
      return tempDirectory;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void setUpDataBaseConfiguration() {
    String databaseName = "lijolo-dev";
    bootstrapPostgresWithTestContainers(databaseName);
  }

  private void bootstrapPostgresWithTestContainers(String databaseName) {
    try {
      Path liquibaseDir                       = copyLiquibaseInitChangelogToTempDir();
      Path liquibaseDirForLoadingUnitTestData = copyLiquibaseForLoadingUnitTestDataChangelogToTempDir();

      if (Objects.isNull(AbstractServiceTest.database)) {
        startDatabase(databaseName,
                      liquibaseDir,
                      liquibaseDirForLoadingUnitTestData);
      }
      AbstractServiceTest.database.getJdbcUrl();
      FileUtils.deleteDirectory(liquibaseDir.toFile());
    } catch (IOException e) {
      System.out.println("SQLException during bootstrapPostgresWithTestContainers -> " + e.getMessage());
    }
  }

  protected Connection getConnection()
      throws SQLException {
    Connection con = DriverManager.getConnection(AbstractServiceTest.database.getJdbcUrl(),
                                                 "test",
                                                 "test");
    con.setAutoCommit(false);
    con.setReadOnly(false);
    con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    return con;
  }

}
