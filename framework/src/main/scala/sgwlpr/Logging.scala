package sgwlpr

import akka.event.LoggingAdapter

trait Logging {
  def log: LoggingAdapter
}
