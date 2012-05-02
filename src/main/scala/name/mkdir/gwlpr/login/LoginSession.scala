package name.mkdir.gwlpr.login

case class LoginSession
{
    var syncCount = 0L

    var email = ""
    var password = ""
    var charName = ""

    override def toString = "LoginSession(%s, %s, %s)".format(email, password, charName)
}
