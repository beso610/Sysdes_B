package models

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

/**
  * MapTaskUser テーブルへの Accessor
  */
@Singleton
class MapTaskUsers @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "task_userinfo_map"

  /**
    * DB上に保存されている全てのタスクを取得する
    * @return
    */
  def list: Seq[MapTaskUser] = Await.result(
    db.run(sql"SELECT task_id, user_name FROM #$table".as[MapTaskUser])
  )

  def findByName(name: String): Option[MapTaskUser] = Await.result(
    db.run(sql"SELECT task_id, user_name FROM #$table WHERE user_name=$name".as[MapTaskUser].headOption)
  )

  def create(mapTaskUser: MapTaskUser): Int = mapTaskUser match {
    case MapTaskUser(task_id, user_name) =>
      Await.result(
        db.run(sqlu"INSERT INTO #$table (task_id, user_name) VALUES ('#$task_id', '#$user_name')")
      )
  }

  def delete(mapTaskUser: MapTaskUser): Int = mapTaskUser match {
    case MapTaskUser(_, user_name) =>
      Await.result(
        db.run(
          sqlu"DELETE FROM #$table WHERE user_name = $user_name"
        )
      )
  }

}
