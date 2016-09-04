package no.nextgentel.oss.akkatools.persistence.jdbcjournal


import javax.sql.DataSource

import com.typesafe.config.Config
import no.nextgentel.oss.akkatools.cluster.ClusterNodeRepo


object JdbcJournalConfig {

  private var name2Config = Map[String, JdbcJournalConfig]()

  def setConfig(configName:String, config:JdbcJournalConfig): Unit ={
    name2Config = name2Config + (configName -> config)
  }

  def getConfig(configName:String):JdbcJournalConfig = {
    name2Config.getOrElse(configName, throw new Exception(s"Configuration with name '$configName' has not ben set."))
  }

  def createJdbcJournalRuntimeData(configName:String): JdbcJournalRuntimeData = {
    val config = getConfig(configName)
    val repo = new StorageRepoImpl(config.dataSource, config.schemaName, config.fatalErrorHandler)
    JdbcJournalRuntimeData(repo, repo, config.persistenceIdParser, config.maxRowsPrRead)
  }

  // Java helper
  def create(dataSource: DataSource, schemaName: String, fatalErrorHandler: JdbcJournalErrorHandler) = JdbcJournalConfig(dataSource, Option(schemaName), fatalErrorHandler)
}

case class JdbcJournalConfig
(
  dataSource: DataSource,
  schemaName: Option[String],
  fatalErrorHandler: JdbcJournalErrorHandler, // The fatalErrorHandler is called when something bad has happened - like getting unique PK key errors - Which is probably a symptom of split brain
  persistenceIdParser:PersistenceIdParser = new PersistenceIdParserImpl('/'),
  maxRowsPrRead: Int = 1000
)



// ------------------------------------------------------------------

case class JdbcJournalRuntimeData( repo:StorageRepo, clusterNodeRepo:ClusterNodeRepo, persistenceIdParser:PersistenceIdParser, maxRowsPrRead:Int)

trait JdbcJournalRuntimeDataExtractor {

  val config: Config

  val configName = config.getString("configName")
  val runtimeData:JdbcJournalRuntimeData = JdbcJournalConfig.createJdbcJournalRuntimeData(configName)
}
