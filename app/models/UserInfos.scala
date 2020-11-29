package models

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

/**
  * UserInfo テーブルへの Accessor
  */
@Singleton
class UserInfos @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "userinfo"

  /**
    * DB上に保存されている全てのタスクを取得する
    * @return
    */
  def list: Seq[UserInfo] = Await.result(
    db.run(sql"SELECT name, password FROM #$table".as[UserInfo])
  )

  def findByName(name: String): Option[UserInfo] = Await.result(
    db.run(sql"SELECT name, password FROM #$table WHERE name=$name".as[UserInfo].headOption)
  )

  def findPassword(name: String): String = Await.result(
    db.run(sql"SELECT password FROM #$table WHERE name=$name".as[String].head)
  )

  def create(userInfo: UserInfo): Int = userInfo match {
    case UserInfo(name, password) =>
      //println("INSERT")
      Await.result(
        db.run(sqlu"INSERT INTO #$table (name, password) VALUES ($name, $password)")
      )
  }

  def update(userInfo: UserInfo): Int = userInfo match {
    case UserInfo(name, password) =>
      //println("UPDATE")
      Await.result(
        db.run(
          sqlu"UPDATE #$table SET password=$password WHERE name = $name"
        )
      )
  }

  def delete(userInfo: UserInfo): Int = userInfo match {
    case UserInfo(name, _) =>
      Await.result(
        db.run(
          sqlu"DELETE FROM #$table WHERE name = $name"
        )
      )
  }

}
