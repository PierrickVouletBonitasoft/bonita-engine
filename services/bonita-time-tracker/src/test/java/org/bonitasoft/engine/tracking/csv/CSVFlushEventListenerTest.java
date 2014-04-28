package org.bonitasoft.engine.tracking.csv;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.AbstractTimeTrackerTest;
import org.bonitasoft.engine.tracking.FlushResult;
import org.bonitasoft.engine.tracking.Record;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.junit.Test;

public class CSVFlushEventListenerTest extends AbstractTimeTrackerTest {

    public void should_work_if_output_folder_is_a_folder() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        new CSVFlushEventListener(System.getProperty("java.io.tmpdir"), ";", logger);
    }

    @Test(expected = RuntimeException.class)
    public void should_fail_if_output_folder_unknown() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        new CSVFlushEventListener("unknownFolder", ";", logger);
    }

    @Test(expected = RuntimeException.class)
    public void should_fail_if_outputfolder_is_a_file() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final File file = new File(System.getProperty("java.io.tmpdir"), "test.txt");
        file.createNewFile();

        new CSVFlushEventListener(file.getAbsolutePath(), ";", logger);
    }

    @Test
    public void testFlushedCsv() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final CSVFlushEventListener csvFlushEventListener = new CSVFlushEventListener(System.getProperty("java.io.tmpdir"), ";",
                logger);
        final List<CSVFlushEventListener> flushEventListeners = Collections.singletonList(csvFlushEventListener);
        final Record rec1 = new Record(System.currentTimeMillis(), "rec", "rec1Desc", 100);
        final Record rec2 = new Record(System.currentTimeMillis(), "rec", "rec2Desc", 200);
        final TimeTracker tracker = new TimeTracker(logger, false, flushEventListeners, 10, 2, "rec");

        tracker.track(rec1);
        tracker.track(rec2);

        final List<FlushResult> flushResults = tracker.flush();
        assertEquals(1, flushResults.size());
        final CSVFlushResult csvFlushResult = (CSVFlushResult) flushResults.get(0);

        final File csvFile = csvFlushResult.getOutputFile();
        final List<List<String>> csvValues = CSVUtil.readCSV(true, csvFile, ";");
        assertEquals(2, csvValues.size());
        checkCSVRecord(rec1, csvValues.get(0));
        checkCSVRecord(rec2, csvValues.get(1));

        final List<Record> records = csvFlushResult.getFlushEvent().getRecords();
        assertEquals(2, records.size());
        checkRecord(rec1, records.get(0));
        checkRecord(rec2, records.get(1));
    }

    private void checkCSVRecord(final Record record, final List<String> csvValues) {
        // timestamp, year, month, day, hour, minute, second, milisecond, duration, name, description]
        assertEquals(11, csvValues.size());

        final long timestamp = record.getTimestamp();
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        final int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        final int minute = cal.get(Calendar.MINUTE);
        final int second = cal.get(Calendar.SECOND);
        final int milisecond = cal.get(Calendar.MILLISECOND);

        assertEquals(timestamp, Long.valueOf(csvValues.get(0)).longValue());
        assertEquals(year, Integer.valueOf(csvValues.get(1)).intValue());
        assertEquals(month, Integer.valueOf(csvValues.get(2)).intValue());
        assertEquals(dayOfMonth, Integer.valueOf(csvValues.get(3)).intValue());
        assertEquals(hourOfDay, Integer.valueOf(csvValues.get(4)).intValue());
        assertEquals(minute, Integer.valueOf(csvValues.get(5)).intValue());
        assertEquals(second, Integer.valueOf(csvValues.get(6)).intValue());
        assertEquals(milisecond, Integer.valueOf(csvValues.get(7)).intValue());

        assertEquals(record.getDuration(), Long.valueOf(csvValues.get(8)).longValue());
        assertEquals(record.getName(), csvValues.get(9));
        assertEquals(record.getDescription(), csvValues.get(10));

    }

}
