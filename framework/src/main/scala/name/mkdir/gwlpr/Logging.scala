package name.mkdir.gwlpr

import akka.event.LoggingAdapter

trait Logging {
  def log: LoggingAdapter
}
