package controllers

import javax.inject.{Inject, Singleton}
import models.{MapTaskUser, MapTaskUsers, Task, Tasks, UserInfo, UserInfos}
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import utility.Digest

/**
  * TodoListコントローラ
  */
@Singleton
class TodoListController @Inject()(tasks: Tasks)(cc: ControllerComponents)(userinfos: UserInfos)(
    mapTaskUsers: MapTaskUsers
) extends AbstractController(cc) {

  /**
    * インデックスページを表示
    */
  def index = Action { implicit request =>
    Ok(views.html.index()).withNewSession
  }

  /**
    ユーザの新規登録
    */
  def newRegister = Action { request =>
    Ok(views.html.todolist.newRegister(request))
  }

  def newRegisterConfirm = Action { request =>
    (for {
      param    <- request.body.asFormUrlEncoded
      name     <- param.get("name").flatMap(_.headOption)
      password <- param.get("password").flatMap(_.headOption)
    } yield {
      val hashPassword = Digest(password)
      userinfos.create(UserInfo(name, hashPassword))
      Redirect("/todolist/tasks").withSession(request.session + ("todolist::name" -> name))
    }).getOrElse[Result](Redirect("/"))
  }

  /**
  ユーザのログイン
    */
  def login = Action { request =>
    Ok(views.html.todolist.login(request))
  }

  def loginConfirm = Action { request =>
    (for {
      param    <- request.body.asFormUrlEncoded
      name     <- param.get("name").flatMap(_.headOption)
      password <- param.get("password").flatMap(_.headOption)

    } yield {
      val hashPassword = Digest(password)
      userinfos.findByName(name) match {
        case Some(user) if (user.password == hashPassword) =>
          Redirect("/todolist/tasks").withSession(request.session + ("todolist::name" -> name))
        case _ => Redirect("/")
      }
    }).getOrElse[Result](Redirect("/"))
  }

  /**
    * マイページの表示
    */
  def mypage = Action { request =>
    (for {
      name <- request.session.get("todolist::name")
    } yield {
      Ok(views.html.todolist.mypage(name))
    }).getOrElse[Result](Redirect("/todolist/tasks"))
  }

  /**
    * パスワードの変更
    */
  def changePassword = Action { request =>
    Ok(views.html.todolist.changePassword(request))
  }

  def change = Action { request =>
    (for {
      name    <- request.session.get("todolist::name")
      param   <- request.body.asFormUrlEncoded
      oldPass <- param.get("old").flatMap(_.headOption)
      newPass <- param.get("new").flatMap(_.headOption)
    } yield {
      val hashOld = Digest(oldPass)
      val hashNew = Digest(newPass)
      if (hashOld == userinfos.findPassword(name)) {
        userinfos.update(UserInfo(name, hashNew))
        Redirect("/todolist/tasks")
      } else {
        Redirect("/")
      }
    }).getOrElse[Result](Redirect("/"))
  }

  /**
    * アカウントの削除
    */
  def delete = Action { request =>
    (for {
      name <- request.session.get("todolist::name")
    } yield {
      userinfos.delete(UserInfo(name))
      mapTaskUsers.delete(MapTaskUser(name))
      Redirect("/")
    }).getOrElse[Result](Redirect("/todolist/mypage"))
  }

  /**
    * 自分が登録したタスクを表示するために絞り込む
    */
  def narrowDownTask(links: Seq[MapTaskUser], name: String): Seq[Task] = {
    var taskSeq = Seq[Task]()
    for (link <- links) {
      if (link.user_name == name) {
        tasks.findByID(link.task_id) match {
          case Some(task) => taskSeq = taskSeq :+ task
        }
      }
    }
    taskSeq
  }

  /**
    * タスクの表示
    */
  def list = Action { request =>
    (for {
      name <- request.session.get("todolist::name")
    } yield {
      val links   = mapTaskUsers.list
      val taskSeq = narrowDownTask(links, name)
      Ok(views.html.todolist.list(taskSeq, name))
    }).getOrElse[Result](Redirect("/"))
  }

  /**
    *　未完了タスクの表示
    */
  def unfinished = Action { request =>
    (for {
      name <- request.session.get("todolist::name")
    } yield {
      val links   = mapTaskUsers.list
      val taskSeq = narrowDownTask(links, name)
      Ok(views.html.todolist.unfinished(taskSeq, name))
    }).getOrElse[Result](Redirect("/"))
  }

  /**
    * タスク詳細
    */
  def entry(id: Int) = Action {
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.todolist.entry(e))
      case None    => NotFound(s"No entry for id=${id}")
    }
  }

  /**
    * タスク編集
    */
  def edit(id: Int) = Action { request =>
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.todolist.edit(e)(request))
      case None    => NotFound(s"No edit for id=${id}")
    }
  }

  /**
    * タスク編集の操作
    */
  def editConfirm(id: Int) = Action { request =>
    (for {
      param       <- request.body.asFormUrlEncoded
      title       <- param.get("title").flatMap(_.headOption)
      description <- param.get("description").flatMap(_.headOption)
      isDone      <- param.get("isDone").flatMap(_.headOption).map(s => s == "true")
    } yield {
      tasks.save(Task(id, title, description, isDone))
      Redirect("/todolist/tasks").withSession(request.session - "csrfToken")
    }).getOrElse[Result](Redirect("/todolist/register"))
  }

  /**
    * タスクの削除
    */
  def editDelete(id: Int) = Action { request =>
    tasks.delete(Task(id))
    Redirect("/todolist/tasks").withSession(request.session - "csrfToken")
  }

  /**
    * タスクの登録
    */
  def startRegistration = Action { request =>
    Ok(views.html.todolist.titleForm(request))
  }

  def registerTitle = Action { request =>
    (for {
      param <- request.body.asFormUrlEncoded
      title <- param.get("title").flatMap(_.headOption)
    } yield {
      Ok(views.html.todolist.descriptionForm(request)).withSession(request.session + ("todolist::title" -> title))
    }).getOrElse[Result](Redirect("/todolist/register"))
  }

  def registerDescription = Action { request =>
    (for {
      title       <- request.session.get("todolist::title")
      param       <- request.body.asFormUrlEncoded
      description <- param.get("description").flatMap(_.headOption)
    } yield {
      val task = Task(title, description)
      Ok(views.html.todolist.confirm(task)(request))
        .withSession(request.session + ("todolist::description" -> description))
    }).getOrElse[Result](Redirect("/todolist/register"))
  }

  /**
    * タスク登録前の確認
    */
  def confirm = Action { request =>
    (for {
      title       <- request.session.get("todolist::title")
      description <- request.session.get("todolist::description")
      name        <- request.session.get("todolist::name")
    } yield {
      tasks.save(Task(title, description))
      tasks.confirmId() match {
        case Some(task_id) => mapTaskUsers.create(MapTaskUser(task_id, name))
        case None          => Redirect("/todolist/register")
      }

      Redirect("/todolist/tasks")
        .withSession(request.session - "csrfToken" - "todolist::title" - "todolist::description")
    }).getOrElse[Result](Redirect("/todolist/register"))
  }

}
