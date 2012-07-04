package sgwlpr.db

case class Account(email: String, password: String, characters: List[Character] = Nil)
