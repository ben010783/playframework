import play.api.templates._

import scala.language.implicitConversions

import scala.collection.JavaConverters._

/*
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package views.html.helper {

  case class FieldElements(id: String, field: play.api.data.Field, input: Html, args: Map[Symbol, Any], lang: play.api.i18n.Lang) {

    def infos(implicit lang: play.api.i18n.Lang): Seq[String] = {
      args.get('_help).map(m => Seq(m.toString)).getOrElse {
        (if (args.get('_showConstraints) match {
          case Some(false) => false
          case _ => true
        }) {
          field.constraints.map(c => play.api.i18n.Messages(c._1, c._2: _*)) ++
            field.format.map(f => play.api.i18n.Messages(f._1, f._2: _*))
        } else Nil)
      }
    }

    def errors(implicit lang: play.api.i18n.Lang): Seq[String] = {
      (args.get('_error) match {
        case Some(Some(play.api.data.FormError(_, message, args))) => Some(Seq(play.api.i18n.Messages(message, args: _*)))
        case _ => None
      }).getOrElse {
        (if (args.get('_showErrors) match {
          case Some(false) => false
          case _ => true
        }) {
          field.errors.map(e => play.api.i18n.Messages(e.message, e.args: _*))
        } else Nil)
      }
    }

    def hasErrors: Boolean = {
      !errors.isEmpty
    }

    def label(implicit lang: play.api.i18n.Lang): Any = {
      args.get('_label).getOrElse(play.api.i18n.Messages(field.name))
    }

    def hasName: Boolean = args.get('_name).isDefined

    def name(implicit lang: play.api.i18n.Lang): Any = {
      args.get('_name).getOrElse(play.api.i18n.Messages(field.name))
    }

  }

  trait FieldConstructor extends NotNull {
    def apply(elts: FieldElements): Html
  }

  object FieldConstructor {

    implicit val defaultField = FieldConstructor(views.html.helper.defaultFieldConstructor.f)

    def apply(f: FieldElements => Html): FieldConstructor = new FieldConstructor {
      def apply(elts: FieldElements) = f(elts)
    }

    implicit def inlineFieldConstructor(f: (FieldElements) => Html) = FieldConstructor(f)
    implicit def templateAsFieldConstructor(t: Template1[FieldElements, Html]) = FieldConstructor(t.render)

  }

  object repeat {

    def apply(field: play.api.data.Field, min: Int = 1)(f: play.api.data.Field => Html) = {
      (0 until math.max(if (field.indexes.isEmpty) 0 else field.indexes.max + 1, min)).map(i => f(field(i + "")))
    }

  }

  object options {

    def apply(options: (String, String)*) = options.toSeq
    def apply(options: Map[String, String]) = options.toSeq
    def apply(options: java.util.Map[String, String]) = options.asScala.toSeq
    def apply(options: List[String]) = options.map(v => v -> v)
    def apply(options: java.util.List[String]) = options.asScala.map(v => v -> v)

  }

}
