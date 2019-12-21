package viper.silver.plugin.standard.decreases.transformation

import viper.silver.ast.{Domain, ExtensionMember, Field, Function, Method, Predicate, Program}
import viper.silver.verifier.AbstractError

import scala.collection.mutable

/**
  * An interface to transform a program (e.g. a program including check methods).
  * Contains utility functions to avoid name conflicts (e.g. when adding a new domain)
  */
trait ProgramManager{

  // original program
  val program: Program

  // maps of all program features including the ones newly created/added
  protected val domains: mutable.Map[String, Domain] = collection.mutable.ListMap[String, Domain](program.domains.map(d => d.name -> d): _*)
  protected val fields: mutable.Map[String, Field] = collection.mutable.ListMap[String, Field](program.fields.map(f => f.name -> f): _*)
  protected val functions: mutable.Map[String, Function] = collection.mutable.ListMap[String, Function](program.functions.map(f => f.name -> f): _*)
  protected val predicates: mutable.Map[String, Predicate] = collection.mutable.ListMap[String, Predicate](program.predicates.map(f => f.name -> f): _*)
  protected val methods: mutable.Map[String, Method] = collection.mutable.ListMap[String, Method](program.methods.map(f => f.name -> f): _*)
  protected val extensions: mutable.Map[String, ExtensionMember] = collection.mutable.ListMap[String, ExtensionMember](program.extensions.map(e => e.name -> e): _*)

  /**
   * Creates a new program with the all the features in the maps.
   * @return new program.
   */
  final protected def generateCheckProgram(): Program = {
    Program(domains.values.toSeq,
      fields.values.toSeq,
      functions.values.toSeq,
      predicates.values.toSeq,
      methods.values.toSeq,
      program.extensions)(program.pos, program.info, program.errT)
  }


  // all names used in the program
  private val usedNames: mutable.Set[String] = collection.mutable.HashSet((
    // TODO: maybe better? program.members.map(m => m.name) (especially if member is extended)
    program.functions
      ++ program.methods
      ++ program.fields
      ++ program.predicates
      ++ program.domains
      ++ program.domains.flatten(_.functions)
    ).map(_.name): _*)


  /**
    * Checks if a name already occurs in the program.
    * @param name to be checked
    * @return true iff the name is used in the program.
    */
  def containsName(name: String): Boolean = {
    usedNames.contains(name)
  }

  /**
    * Creates a unique name for the program and adds it to the names already used in the program.
    * Should be used whenever a field, method, predicate, etc. is added to the needed fields.
    * @param name which is wanted
    * @return a name which is unique in the program
    */
  def uniqueName(name: String): String = {
    var i = 1
    var newName = name
    while(containsName(newName)){
      newName = name + i
      i += 1
    }
    usedNames.add(newName)
    newName
  }
}

trait ErrorReporter {
  // to report any errors occurring during transformation
  val reportError: AbstractError => Unit
}