package controllers.hello

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class Helloworld @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def display = Action { implicit request =>
    // 200 OK ステータスで app/views/hello/display.scala.html をレンダリングする
    Ok(views.html.hello.display("Hello world!"))
  }
}
