package sgwlpr.db

case class Account(email: Option[String] = None, characters: List[Character] = Nil)
