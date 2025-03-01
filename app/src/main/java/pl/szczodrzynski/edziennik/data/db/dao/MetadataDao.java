/*
 * Copyright (c) Kacper Ziubryniewicz 2020-1-6
 */

package pl.szczodrzynski.edziennik.data.db.dao;

import static pl.szczodrzynski.edziennik.data.db.entity.Metadata.TYPE_ANNOUNCEMENT;
import static pl.szczodrzynski.edziennik.data.db.entity.Metadata.TYPE_ATTENDANCE;
import static pl.szczodrzynski.edziennik.data.db.entity.Metadata.TYPE_EVENT;
import static pl.szczodrzynski.edziennik.data.db.entity.Metadata.TYPE_GRADE;
import static pl.szczodrzynski.edziennik.data.db.entity.Metadata.TYPE_HOMEWORK;
import static pl.szczodrzynski.edziennik.data.db.entity.Metadata.TYPE_LESSON_CHANGE;
import static pl.szczodrzynski.edziennik.data.db.entity.Metadata.TYPE_MESSAGE;
import static pl.szczodrzynski.edziennik.data.db.entity.Metadata.TYPE_NOTICE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import pl.szczodrzynski.edziennik.data.db.entity.Announcement;
import pl.szczodrzynski.edziennik.data.db.entity.Attendance;
import pl.szczodrzynski.edziennik.data.db.entity.Event;
import pl.szczodrzynski.edziennik.data.db.entity.Grade;
import pl.szczodrzynski.edziennik.data.db.entity.Message;
import pl.szczodrzynski.edziennik.data.db.entity.Metadata;
import pl.szczodrzynski.edziennik.data.db.entity.Notice;
import pl.szczodrzynski.edziennik.data.db.full.LessonFull;
import pl.szczodrzynski.edziennik.utils.models.UnreadCounter;

@Dao
public abstract class MetadataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long add(Metadata metadata);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void addAllIgnore(List<Metadata> metadataList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void addAllReplace(List<Metadata> metadataList);

    @Query("UPDATE metadata SET seen = :seen WHERE thingId = :thingId AND thingType = :thingType AND profileId = :profileId")
    abstract void updateSeen(int profileId, int thingType, long thingId, boolean seen);

    @Query("UPDATE metadata SET notified = :notified WHERE thingId = :thingId AND thingType = :thingType AND profileId = :profileId")
    abstract void updateNotified(int profileId, int thingType, long thingId, boolean notified);



    @Transaction
    public void setSeen(List<Metadata> metadataList) {
        for (Metadata metadata: metadataList) {
            if (add(metadata) == -1) {
                updateSeen(metadata.profileId, metadata.thingType, metadata.thingId, metadata.seen);
            }
        }
    }

    @Transaction
    public void setSeen(int profileId, Object o, boolean seen) {
        if (o instanceof Grade) {
            if (add(new Metadata(profileId, TYPE_GRADE, ((Grade) o).getId(), seen, false)) == -1) {
                updateSeen(profileId, TYPE_GRADE, ((Grade) o).getId(), seen);
            }
        }
        if (o instanceof Attendance) {
            if (add(new Metadata(profileId, TYPE_ATTENDANCE, ((Attendance) o).getId(), seen, false)) == -1) {
                updateSeen(profileId, TYPE_ATTENDANCE, ((Attendance) o).getId(), seen);
            }
        }
        if (o instanceof Notice) {
            if (add(new Metadata(profileId, TYPE_NOTICE, ((Notice) o).getId(), seen, false)) == -1) {
                updateSeen(profileId, TYPE_NOTICE, ((Notice) o).getId(), seen);
            }
        }
        if (o instanceof Event) {
            if (add(new Metadata(profileId, ((Event) o).isHomework() ? TYPE_HOMEWORK : TYPE_EVENT, ((Event) o).getId(), seen, false)) == -1) {
                updateSeen(profileId, ((Event) o).isHomework() ? TYPE_HOMEWORK : TYPE_EVENT, ((Event) o).getId(), seen);
            }
        }
        if (o instanceof LessonFull) {
            if (add(new Metadata(profileId, TYPE_LESSON_CHANGE, ((LessonFull) o).getId(), seen, false)) == -1) {
                updateSeen(profileId, TYPE_LESSON_CHANGE, ((LessonFull) o).getId(), seen);
            }
        }
        if (o instanceof Announcement) {
            if (add(new Metadata(profileId, TYPE_ANNOUNCEMENT, ((Announcement) o).getId(), seen, false)) == -1) {
                updateSeen(profileId, TYPE_ANNOUNCEMENT, ((Announcement) o).getId(), seen);
            }
        }
        if (o instanceof Message) {
            if (add(new Metadata(profileId, TYPE_MESSAGE, ((Message) o).getId(), seen, false)) == -1) {
                updateSeen(profileId, TYPE_MESSAGE, ((Message) o).getId(), seen);
            }
        }
    }

    @Transaction
    public void setNotified(int profileId, Object o, boolean notified) {
        if (o instanceof Grade) {
            if (add(new Metadata(profileId, TYPE_GRADE, ((Grade) o).getId(), false, notified)) == -1) {
                updateNotified(profileId, TYPE_GRADE, ((Grade) o).getId(), notified);
            }
        }
        if (o instanceof Attendance) {
            if (add(new Metadata(profileId, TYPE_ATTENDANCE, ((Attendance) o).getId(), false, notified)) == -1) {
                updateNotified(profileId, TYPE_ATTENDANCE, ((Attendance) o).getId(), notified);
            }
        }
        if (o instanceof Notice) {
            if (add(new Metadata(profileId, TYPE_NOTICE, ((Notice) o).getId(), false, notified)) == -1) {
                updateNotified(profileId, TYPE_NOTICE, ((Notice) o).getId(), notified);
            }
        }
        if (o instanceof Event) {
            if (add(new Metadata(profileId, ((Event) o).isHomework() ? TYPE_HOMEWORK : TYPE_EVENT, ((Event) o).getId(), false, notified)) == -1) {
                updateNotified(profileId, ((Event) o).isHomework() ? TYPE_HOMEWORK : TYPE_EVENT, ((Event) o).getId(), notified);
            }
        }
        if (o instanceof LessonFull) {
            if (add(new Metadata(profileId, TYPE_LESSON_CHANGE, ((LessonFull) o).getId(), false, notified)) == -1) {
                updateNotified(profileId, TYPE_LESSON_CHANGE, ((LessonFull) o).getId(), notified);
            }
        }
        if (o instanceof Announcement) {
            if (add(new Metadata(profileId, TYPE_ANNOUNCEMENT, ((Announcement) o).getId(), false, notified)) == -1) {
                updateNotified(profileId, TYPE_ANNOUNCEMENT, ((Announcement) o).getId(), notified);
            }
        }
        if (o instanceof Message) {
            if (add(new Metadata(profileId, TYPE_MESSAGE, ((Message) o).getId(), false, notified)) == -1) {
                updateNotified(profileId, TYPE_MESSAGE, ((Message) o).getId(), notified);
            }
        }
    }

    @Transaction
    public void setBoth(int profileId, Event o, boolean seen, boolean notified, long addedDate) {
        if (o != null) {
            if (add(new Metadata(profileId, o.isHomework() ? TYPE_HOMEWORK : TYPE_EVENT, o.getId(), seen, notified)) == -1) {
                updateSeen(profileId, o.isHomework() ? TYPE_HOMEWORK : TYPE_EVENT, o.getId(), seen);
                updateNotified(profileId, o.isHomework() ? TYPE_HOMEWORK : TYPE_EVENT, o.getId(), notified);
            }
        }
    }



    @Query("UPDATE metadata SET seen = :seen WHERE profileId = :profileId AND thingType = :thingType")
    public abstract void setAllSeen(int profileId, int thingType, boolean seen);

    @Query("UPDATE metadata SET notified = :notified WHERE profileId = :profileId AND thingType = :thingType")
    public abstract void setAllNotified(int profileId, int thingType, boolean notified);

    @Query("UPDATE metadata SET seen = :seen WHERE profileId = :profileId")
    public abstract void setAllSeen(int profileId, boolean seen);

    @Query("UPDATE metadata SET seen = :seen WHERE profileId = :profileId AND thingType != " + TYPE_MESSAGE)
    public abstract void setAllSeenExceptMessages(int profileId, boolean seen);

    @Query("UPDATE metadata SET seen = :seen WHERE profileId = :profileId AND thingType != " + TYPE_MESSAGE + " AND thingType != " + TYPE_ANNOUNCEMENT)
    public abstract void setAllSeenExceptMessagesAndAnnouncements(int profileId, boolean seen);

    @Query("UPDATE metadata SET notified = :notified WHERE profileId = :profileId")
    public abstract void setAllNotified(int profileId, boolean notified);

    @Query("UPDATE metadata SET notified = :notified")
    public abstract void setAllNotified(boolean notified);



    @Query("SELECT count() FROM metadata WHERE profileId = :profileId AND thingType = :thingType AND seen = 0")
    public abstract LiveData<Integer> countUnseen(int profileId, int thingType);

    @Query("SELECT count() FROM metadata WHERE profileId = :profileId AND thingType = :thingType AND seen = 0")
    public abstract Integer countUnseenNow(int profileId, int thingType);

    @Query("SELECT count() FROM metadata WHERE profileId = :profileId AND seen = 0")
    public abstract LiveData<Integer> countUnseen(int profileId);

    @Query("SELECT count() FROM metadata WHERE profileId = :profileId AND seen = 0")
    public abstract Integer countUnseenNow(int profileId);

    @Query("SELECT count() FROM metadata WHERE seen = 0")
    public abstract LiveData<Integer> countUnseen();



    @Query("DELETE FROM metadata WHERE profileId = :profileId AND thingType = :thingType AND thingId = :thingId")
    public abstract void delete(int profileId, int thingType, long thingId);

    @Query("DELETE FROM metadata WHERE profileId = :profileId")
    public abstract void deleteAll(int profileId);



    @Query("SELECT profileId, thingType, COUNT(thingId) AS count FROM metadata WHERE seen = 0 GROUP BY profileId, thingType")
    public abstract LiveData<List<UnreadCounter>> getUnreadCounts();



    @Query("DELETE FROM metadata WHERE profileId = :profileId AND thingType = "+TYPE_GRADE+" AND thingId NOT IN (SELECT gradeId FROM grades WHERE profileId = :profileId);")
    public abstract void deleteUnusedGrades(int profileId);

    @Query("DELETE FROM metadata WHERE profileId = :profileId AND thingType = "+TYPE_NOTICE+" AND thingId NOT IN (SELECT noticeId FROM notices WHERE profileId = :profileId);")
    public abstract void deleteUnusedNotices(int profileId);

    @Query("DELETE FROM metadata WHERE profileId = :profileId AND thingType = "+TYPE_ATTENDANCE+" AND thingId NOT IN (SELECT attendanceId FROM attendances WHERE profileId = :profileId);")
    public abstract void deleteUnusedAttendance(int profileId);

    @Query("DELETE FROM metadata WHERE profileId = :profileId AND thingType = "+TYPE_EVENT+" AND thingId NOT IN (SELECT eventId FROM events WHERE profileId = :profileId AND eventType != -1);")
    public abstract void deleteUnusedEvents(int profileId);

    @Query("DELETE FROM metadata WHERE profileId = :profileId AND thingType = "+TYPE_HOMEWORK+" AND thingId NOT IN (SELECT eventId FROM events WHERE profileId = :profileId AND eventType = -1);")
    public abstract void deleteUnusedHomework(int profileId);

    @Query("DELETE FROM metadata WHERE profileId = :profileId AND thingType = "+TYPE_ANNOUNCEMENT+" AND thingId NOT IN (SELECT announcementId FROM announcements WHERE profileId = :profileId);")
    public abstract void deleteUnusedAnnouncements(int profileId);

    @Query("DELETE FROM metadata WHERE profileId = :profileId AND thingType = "+TYPE_MESSAGE+" AND thingId NOT IN (SELECT messageId FROM messages WHERE profileId = :profileId);")
    public abstract void deleteUnusedMessages(int profileId);

    @Transaction
    public void deleteUnused(int profileId) {
        deleteUnusedGrades(profileId);
        deleteUnusedNotices(profileId);
        deleteUnusedAttendance(profileId);
        deleteUnusedEvents(profileId);
        deleteUnusedHomework(profileId);
        deleteUnusedAnnouncements(profileId);
        deleteUnusedMessages(profileId);
    }
}
