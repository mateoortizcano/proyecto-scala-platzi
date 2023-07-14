package repository

import models.{Movie, MovieTable}

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.{AbstractController, ControllerComponents}
import slick.jdbc.JdbcProfile
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}
/*
  Clase repository donde irán las consultas
 */
class MovieRepository  @Inject() (
                                   protected val dbConfigProvider: DatabaseConfigProvider,
                                   cc: ControllerComponents
                                 )(implicit ec: ExecutionContext)
extends AbstractController(cc)
with HasDatabaseConfigProvider[JdbcProfile] {
  private lazy val movieQuery = TableQuery[MovieTable]
  // Esto va dentro de la clase MovieRepository

  /**
   * Función de ayuda para crear la tabla si ésta
   * aún no existe en la base de datos.
   *
   * @return
   */
  def dbInit: Future[Unit] = {
    // Definición de la sentencia SQL de creación del schema
    val createSchema = movieQuery.schema.createIfNotExists
    // db.run Ejecuta una sentencia SQL, devolviendo un Future
    db.run(createSchema)
  }

  def getAll = {
    val q = movieQuery.sortBy(_.id)
    db.run(q.result)
  }

  def getOne(id: String) = {
    val q = movieQuery.filter(_.id === id)
    db.run(q.result.headOption)
  }

  def create(movie: Movie) = {
    val insert = movieQuery += movie
    db.run(insert)
      .flatMap(_ => getOne(movie.id.getOrElse("")))
  }

  def update(id: String, movie: Movie) = {
    val q = movieQuery.filter(_.id === movie.id && movie.id.contains(id))
    val update = q.update(movie)
    db.run(update)
      .flatMap(_ => db.run(q.result.headOption))
  }

  def delete(id: String) = {
    val q = movieQuery.filter(_.id === id)
    for {
      objeto <- db.run(q.result.headOption)
      _ <- db.run(q.delete)
    } yield objeto
  }
}


