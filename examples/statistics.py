# Examples of using The Grinder statistics API with standard
# statistics.

from net.grinder.script import Test
from net.grinder.plugin.http import HTTPRequest

log = grinder.logger.output
logError = grinder.logger.error

test1 = Test(1, "Request resource")
request1 = test1.wrap(HTTPRequest())

class TestRunner:
    def __call__(self):
        statistics = grinder.statistics

        # Example 1. You can get the time of the last test as follows.
        result = request1.GET("http://localhost:7001/")
        log("The last test took %d milliseconds" % statistics.time)

        
        # Example 2. Normally test results are reported automatically
        # when the test returns. If you want to alter the statistics
        # after a test has completed, you must disable the automatic
        # reporting before performing the test. This only affects the
        # current worker thread.        
        statistics.autoReport = 0

        result = request1.GET("http://localhost:7001/")

        if statistics.time > 5:
            # We set success = 0 to mark the test as a failure. This
            # discards the transaction time to comply with the
            # convention of only recording time for successful tests,
            # so lets log the actual time to the error log.
            logError("The last test took too long (%d milliseconds)" %
                     statistics.time)
            statistics.success = 0

        # If you set autoReport = 0 you must remember to call report()
        # or the current statistics will not be sent until the thread
        # begins the next test .        
        statistics.report()

        # Now we turn auto-reporting back on.
        statistics.autoReport = 1
