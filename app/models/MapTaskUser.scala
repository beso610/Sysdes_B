package models

/**
  * Domain model of userinfo
  *
  * @param task_id    タスクid
  * @param user_name  ユーザ名
  */
case class MapTaskUser(task_id: Int, user_name: String)

object MapTaskUser extends DomainModel[MapTaskUser] {
  import slick.jdbc.GetResult
  implicit def getResult: GetResult[MapTaskUser] = GetResult(
    r => MapTaskUser(r.nextInt, r.nextString)
  )
  def apply(user_name: String): MapTaskUser = MapTaskUser(0, user_name)

}
