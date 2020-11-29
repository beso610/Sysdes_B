package models

/**
  * Domain model of userinfo
  * @param name          ユーザ名
  * @param password      パスワード
  */
case class UserInfo(name: String, password: String)

object UserInfo extends DomainModel[UserInfo] {
  import slick.jdbc.GetResult
  implicit def getResult: GetResult[UserInfo] = GetResult(
    r => UserInfo(r.nextString, r.nextString)
  )
  def apply(name: String): UserInfo = UserInfo(name, "")

}
