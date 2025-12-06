package com.bes2.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.bes2.data.model.ReviewItemEntity;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReviewItemDao_Impl implements ReviewItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReviewItemEntity> __insertionAdapterOfReviewItemEntity;

  private final EntityDeletionOrUpdateAdapter<ReviewItemEntity> __updateAdapterOfReviewItemEntity;

  public ReviewItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReviewItemEntity = new EntityInsertionAdapter<ReviewItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `review_items` (`id`,`uri`,`filePath`,`timestamp`,`status`,`source_type`,`pHash`,`nimaScore`,`musiqScore`,`blurScore`,`exposureScore`,`areEyesClosed`,`smilingProbability`,`faceEmbedding`,`embedding`,`cluster_id`,`isUploaded`,`isSelectedByUser`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReviewItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUri());
        statement.bindString(3, entity.getFilePath());
        statement.bindLong(4, entity.getTimestamp());
        statement.bindString(5, entity.getStatus());
        statement.bindString(6, entity.getSource_type());
        if (entity.getPHash() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPHash());
        }
        if (entity.getNimaScore() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getNimaScore());
        }
        if (entity.getMusiqScore() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getMusiqScore());
        }
        if (entity.getBlurScore() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getBlurScore());
        }
        if (entity.getExposureScore() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getExposureScore());
        }
        final Integer _tmp = entity.getAreEyesClosed() == null ? null : (entity.getAreEyesClosed() ? 1 : 0);
        if (_tmp == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, _tmp);
        }
        if (entity.getSmilingProbability() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getSmilingProbability());
        }
        if (entity.getFaceEmbedding() == null) {
          statement.bindNull(14);
        } else {
          statement.bindBlob(14, entity.getFaceEmbedding());
        }
        if (entity.getEmbedding() == null) {
          statement.bindNull(15);
        } else {
          statement.bindBlob(15, entity.getEmbedding());
        }
        if (entity.getCluster_id() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getCluster_id());
        }
        final int _tmp_1 = entity.isUploaded() ? 1 : 0;
        statement.bindLong(17, _tmp_1);
        final int _tmp_2 = entity.isSelectedByUser() ? 1 : 0;
        statement.bindLong(18, _tmp_2);
      }
    };
    this.__updateAdapterOfReviewItemEntity = new EntityDeletionOrUpdateAdapter<ReviewItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `review_items` SET `id` = ?,`uri` = ?,`filePath` = ?,`timestamp` = ?,`status` = ?,`source_type` = ?,`pHash` = ?,`nimaScore` = ?,`musiqScore` = ?,`blurScore` = ?,`exposureScore` = ?,`areEyesClosed` = ?,`smilingProbability` = ?,`faceEmbedding` = ?,`embedding` = ?,`cluster_id` = ?,`isUploaded` = ?,`isSelectedByUser` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReviewItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUri());
        statement.bindString(3, entity.getFilePath());
        statement.bindLong(4, entity.getTimestamp());
        statement.bindString(5, entity.getStatus());
        statement.bindString(6, entity.getSource_type());
        if (entity.getPHash() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPHash());
        }
        if (entity.getNimaScore() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getNimaScore());
        }
        if (entity.getMusiqScore() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getMusiqScore());
        }
        if (entity.getBlurScore() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getBlurScore());
        }
        if (entity.getExposureScore() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getExposureScore());
        }
        final Integer _tmp = entity.getAreEyesClosed() == null ? null : (entity.getAreEyesClosed() ? 1 : 0);
        if (_tmp == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, _tmp);
        }
        if (entity.getSmilingProbability() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getSmilingProbability());
        }
        if (entity.getFaceEmbedding() == null) {
          statement.bindNull(14);
        } else {
          statement.bindBlob(14, entity.getFaceEmbedding());
        }
        if (entity.getEmbedding() == null) {
          statement.bindNull(15);
        } else {
          statement.bindBlob(15, entity.getEmbedding());
        }
        if (entity.getCluster_id() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getCluster_id());
        }
        final int _tmp_1 = entity.isUploaded() ? 1 : 0;
        statement.bindLong(17, _tmp_1);
        final int _tmp_2 = entity.isSelectedByUser() ? 1 : 0;
        statement.bindLong(18, _tmp_2);
        statement.bindLong(19, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final ReviewItemEntity item, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReviewItemEntity.insertAndReturnId(item);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<ReviewItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReviewItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ReviewItemEntity item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReviewItemEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ReviewItemEntity>> getItemsBySourceTypeFlow(final String sourceType) {
    final String _sql = "SELECT * FROM review_items WHERE source_type = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sourceType);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"review_items"}, new Callable<List<ReviewItemEntity>>() {
      @Override
      @NonNull
      public List<ReviewItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "source_type");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfNimaScore = CursorUtil.getColumnIndexOrThrow(_cursor, "nimaScore");
          final int _cursorIndexOfMusiqScore = CursorUtil.getColumnIndexOrThrow(_cursor, "musiqScore");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfExposureScore = CursorUtil.getColumnIndexOrThrow(_cursor, "exposureScore");
          final int _cursorIndexOfAreEyesClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "areEyesClosed");
          final int _cursorIndexOfSmilingProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "smilingProbability");
          final int _cursorIndexOfFaceEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "faceEmbedding");
          final int _cursorIndexOfEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "embedding");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "cluster_id");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final List<ReviewItemEntity> _result = new ArrayList<ReviewItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpSource_type;
            _tmpSource_type = _cursor.getString(_cursorIndexOfSourceType);
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Double _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getDouble(_cursorIndexOfNimaScore);
            }
            final Float _tmpMusiqScore;
            if (_cursor.isNull(_cursorIndexOfMusiqScore)) {
              _tmpMusiqScore = null;
            } else {
              _tmpMusiqScore = _cursor.getFloat(_cursorIndexOfMusiqScore);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final Float _tmpExposureScore;
            if (_cursor.isNull(_cursorIndexOfExposureScore)) {
              _tmpExposureScore = null;
            } else {
              _tmpExposureScore = _cursor.getFloat(_cursorIndexOfExposureScore);
            }
            final Boolean _tmpAreEyesClosed;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp == null ? null : _tmp != 0;
            final Float _tmpSmilingProbability;
            if (_cursor.isNull(_cursorIndexOfSmilingProbability)) {
              _tmpSmilingProbability = null;
            } else {
              _tmpSmilingProbability = _cursor.getFloat(_cursorIndexOfSmilingProbability);
            }
            final byte[] _tmpFaceEmbedding;
            if (_cursor.isNull(_cursorIndexOfFaceEmbedding)) {
              _tmpFaceEmbedding = null;
            } else {
              _tmpFaceEmbedding = _cursor.getBlob(_cursorIndexOfFaceEmbedding);
            }
            final byte[] _tmpEmbedding;
            if (_cursor.isNull(_cursorIndexOfEmbedding)) {
              _tmpEmbedding = null;
            } else {
              _tmpEmbedding = _cursor.getBlob(_cursorIndexOfEmbedding);
            }
            final String _tmpCluster_id;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpCluster_id = null;
            } else {
              _tmpCluster_id = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsUploaded;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_1 != 0;
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            _item = new ReviewItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpSource_type,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpCluster_id,_tmpIsUploaded,_tmpIsSelectedByUser);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getItemsBySourceType(final String sourceType,
      final Continuation<? super List<ReviewItemEntity>> $completion) {
    final String _sql = "SELECT * FROM review_items WHERE source_type = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sourceType);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewItemEntity>>() {
      @Override
      @NonNull
      public List<ReviewItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "source_type");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfNimaScore = CursorUtil.getColumnIndexOrThrow(_cursor, "nimaScore");
          final int _cursorIndexOfMusiqScore = CursorUtil.getColumnIndexOrThrow(_cursor, "musiqScore");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfExposureScore = CursorUtil.getColumnIndexOrThrow(_cursor, "exposureScore");
          final int _cursorIndexOfAreEyesClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "areEyesClosed");
          final int _cursorIndexOfSmilingProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "smilingProbability");
          final int _cursorIndexOfFaceEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "faceEmbedding");
          final int _cursorIndexOfEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "embedding");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "cluster_id");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final List<ReviewItemEntity> _result = new ArrayList<ReviewItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpSource_type;
            _tmpSource_type = _cursor.getString(_cursorIndexOfSourceType);
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Double _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getDouble(_cursorIndexOfNimaScore);
            }
            final Float _tmpMusiqScore;
            if (_cursor.isNull(_cursorIndexOfMusiqScore)) {
              _tmpMusiqScore = null;
            } else {
              _tmpMusiqScore = _cursor.getFloat(_cursorIndexOfMusiqScore);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final Float _tmpExposureScore;
            if (_cursor.isNull(_cursorIndexOfExposureScore)) {
              _tmpExposureScore = null;
            } else {
              _tmpExposureScore = _cursor.getFloat(_cursorIndexOfExposureScore);
            }
            final Boolean _tmpAreEyesClosed;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp == null ? null : _tmp != 0;
            final Float _tmpSmilingProbability;
            if (_cursor.isNull(_cursorIndexOfSmilingProbability)) {
              _tmpSmilingProbability = null;
            } else {
              _tmpSmilingProbability = _cursor.getFloat(_cursorIndexOfSmilingProbability);
            }
            final byte[] _tmpFaceEmbedding;
            if (_cursor.isNull(_cursorIndexOfFaceEmbedding)) {
              _tmpFaceEmbedding = null;
            } else {
              _tmpFaceEmbedding = _cursor.getBlob(_cursorIndexOfFaceEmbedding);
            }
            final byte[] _tmpEmbedding;
            if (_cursor.isNull(_cursorIndexOfEmbedding)) {
              _tmpEmbedding = null;
            } else {
              _tmpEmbedding = _cursor.getBlob(_cursorIndexOfEmbedding);
            }
            final String _tmpCluster_id;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpCluster_id = null;
            } else {
              _tmpCluster_id = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsUploaded;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_1 != 0;
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            _item = new ReviewItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpSource_type,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpCluster_id,_tmpIsUploaded,_tmpIsSelectedByUser);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getItemsBySourceAndStatus(final String sourceType, final String status,
      final Continuation<? super List<ReviewItemEntity>> $completion) {
    final String _sql = "SELECT * FROM review_items WHERE source_type = ? AND status = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sourceType);
    _argIndex = 2;
    _statement.bindString(_argIndex, status);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewItemEntity>>() {
      @Override
      @NonNull
      public List<ReviewItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "source_type");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfNimaScore = CursorUtil.getColumnIndexOrThrow(_cursor, "nimaScore");
          final int _cursorIndexOfMusiqScore = CursorUtil.getColumnIndexOrThrow(_cursor, "musiqScore");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfExposureScore = CursorUtil.getColumnIndexOrThrow(_cursor, "exposureScore");
          final int _cursorIndexOfAreEyesClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "areEyesClosed");
          final int _cursorIndexOfSmilingProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "smilingProbability");
          final int _cursorIndexOfFaceEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "faceEmbedding");
          final int _cursorIndexOfEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "embedding");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "cluster_id");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final List<ReviewItemEntity> _result = new ArrayList<ReviewItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpSource_type;
            _tmpSource_type = _cursor.getString(_cursorIndexOfSourceType);
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Double _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getDouble(_cursorIndexOfNimaScore);
            }
            final Float _tmpMusiqScore;
            if (_cursor.isNull(_cursorIndexOfMusiqScore)) {
              _tmpMusiqScore = null;
            } else {
              _tmpMusiqScore = _cursor.getFloat(_cursorIndexOfMusiqScore);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final Float _tmpExposureScore;
            if (_cursor.isNull(_cursorIndexOfExposureScore)) {
              _tmpExposureScore = null;
            } else {
              _tmpExposureScore = _cursor.getFloat(_cursorIndexOfExposureScore);
            }
            final Boolean _tmpAreEyesClosed;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp == null ? null : _tmp != 0;
            final Float _tmpSmilingProbability;
            if (_cursor.isNull(_cursorIndexOfSmilingProbability)) {
              _tmpSmilingProbability = null;
            } else {
              _tmpSmilingProbability = _cursor.getFloat(_cursorIndexOfSmilingProbability);
            }
            final byte[] _tmpFaceEmbedding;
            if (_cursor.isNull(_cursorIndexOfFaceEmbedding)) {
              _tmpFaceEmbedding = null;
            } else {
              _tmpFaceEmbedding = _cursor.getBlob(_cursorIndexOfFaceEmbedding);
            }
            final byte[] _tmpEmbedding;
            if (_cursor.isNull(_cursorIndexOfEmbedding)) {
              _tmpEmbedding = null;
            } else {
              _tmpEmbedding = _cursor.getBlob(_cursorIndexOfEmbedding);
            }
            final String _tmpCluster_id;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpCluster_id = null;
            } else {
              _tmpCluster_id = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsUploaded;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_1 != 0;
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            _item = new ReviewItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpSource_type,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpCluster_id,_tmpIsUploaded,_tmpIsSelectedByUser);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getItemsByClusterId(final String clusterId,
      final Continuation<? super List<ReviewItemEntity>> $completion) {
    final String _sql = "SELECT * FROM review_items WHERE cluster_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, clusterId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewItemEntity>>() {
      @Override
      @NonNull
      public List<ReviewItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "source_type");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfNimaScore = CursorUtil.getColumnIndexOrThrow(_cursor, "nimaScore");
          final int _cursorIndexOfMusiqScore = CursorUtil.getColumnIndexOrThrow(_cursor, "musiqScore");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfExposureScore = CursorUtil.getColumnIndexOrThrow(_cursor, "exposureScore");
          final int _cursorIndexOfAreEyesClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "areEyesClosed");
          final int _cursorIndexOfSmilingProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "smilingProbability");
          final int _cursorIndexOfFaceEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "faceEmbedding");
          final int _cursorIndexOfEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "embedding");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "cluster_id");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final List<ReviewItemEntity> _result = new ArrayList<ReviewItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpSource_type;
            _tmpSource_type = _cursor.getString(_cursorIndexOfSourceType);
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Double _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getDouble(_cursorIndexOfNimaScore);
            }
            final Float _tmpMusiqScore;
            if (_cursor.isNull(_cursorIndexOfMusiqScore)) {
              _tmpMusiqScore = null;
            } else {
              _tmpMusiqScore = _cursor.getFloat(_cursorIndexOfMusiqScore);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final Float _tmpExposureScore;
            if (_cursor.isNull(_cursorIndexOfExposureScore)) {
              _tmpExposureScore = null;
            } else {
              _tmpExposureScore = _cursor.getFloat(_cursorIndexOfExposureScore);
            }
            final Boolean _tmpAreEyesClosed;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp == null ? null : _tmp != 0;
            final Float _tmpSmilingProbability;
            if (_cursor.isNull(_cursorIndexOfSmilingProbability)) {
              _tmpSmilingProbability = null;
            } else {
              _tmpSmilingProbability = _cursor.getFloat(_cursorIndexOfSmilingProbability);
            }
            final byte[] _tmpFaceEmbedding;
            if (_cursor.isNull(_cursorIndexOfFaceEmbedding)) {
              _tmpFaceEmbedding = null;
            } else {
              _tmpFaceEmbedding = _cursor.getBlob(_cursorIndexOfFaceEmbedding);
            }
            final byte[] _tmpEmbedding;
            if (_cursor.isNull(_cursorIndexOfEmbedding)) {
              _tmpEmbedding = null;
            } else {
              _tmpEmbedding = _cursor.getBlob(_cursorIndexOfEmbedding);
            }
            final String _tmpCluster_id;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpCluster_id = null;
            } else {
              _tmpCluster_id = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsUploaded;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_1 != 0;
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            _item = new ReviewItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpSource_type,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpCluster_id,_tmpIsUploaded,_tmpIsSelectedByUser);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getNewDietItems(final Continuation<? super List<ReviewItemEntity>> $completion) {
    final String _sql = "SELECT * FROM review_items WHERE status = 'NEW' AND source_type = 'DIET'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewItemEntity>>() {
      @Override
      @NonNull
      public List<ReviewItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "source_type");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfNimaScore = CursorUtil.getColumnIndexOrThrow(_cursor, "nimaScore");
          final int _cursorIndexOfMusiqScore = CursorUtil.getColumnIndexOrThrow(_cursor, "musiqScore");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfExposureScore = CursorUtil.getColumnIndexOrThrow(_cursor, "exposureScore");
          final int _cursorIndexOfAreEyesClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "areEyesClosed");
          final int _cursorIndexOfSmilingProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "smilingProbability");
          final int _cursorIndexOfFaceEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "faceEmbedding");
          final int _cursorIndexOfEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "embedding");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "cluster_id");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final List<ReviewItemEntity> _result = new ArrayList<ReviewItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpSource_type;
            _tmpSource_type = _cursor.getString(_cursorIndexOfSourceType);
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Double _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getDouble(_cursorIndexOfNimaScore);
            }
            final Float _tmpMusiqScore;
            if (_cursor.isNull(_cursorIndexOfMusiqScore)) {
              _tmpMusiqScore = null;
            } else {
              _tmpMusiqScore = _cursor.getFloat(_cursorIndexOfMusiqScore);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final Float _tmpExposureScore;
            if (_cursor.isNull(_cursorIndexOfExposureScore)) {
              _tmpExposureScore = null;
            } else {
              _tmpExposureScore = _cursor.getFloat(_cursorIndexOfExposureScore);
            }
            final Boolean _tmpAreEyesClosed;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp == null ? null : _tmp != 0;
            final Float _tmpSmilingProbability;
            if (_cursor.isNull(_cursorIndexOfSmilingProbability)) {
              _tmpSmilingProbability = null;
            } else {
              _tmpSmilingProbability = _cursor.getFloat(_cursorIndexOfSmilingProbability);
            }
            final byte[] _tmpFaceEmbedding;
            if (_cursor.isNull(_cursorIndexOfFaceEmbedding)) {
              _tmpFaceEmbedding = null;
            } else {
              _tmpFaceEmbedding = _cursor.getBlob(_cursorIndexOfFaceEmbedding);
            }
            final byte[] _tmpEmbedding;
            if (_cursor.isNull(_cursorIndexOfEmbedding)) {
              _tmpEmbedding = null;
            } else {
              _tmpEmbedding = _cursor.getBlob(_cursorIndexOfEmbedding);
            }
            final String _tmpCluster_id;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpCluster_id = null;
            } else {
              _tmpCluster_id = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsUploaded;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_1 != 0;
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            _item = new ReviewItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpSource_type,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpCluster_id,_tmpIsUploaded,_tmpIsSelectedByUser);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getNewDietItemsBatch(final int limit,
      final Continuation<? super List<ReviewItemEntity>> $completion) {
    final String _sql = "SELECT * FROM review_items WHERE status = 'NEW' AND source_type = 'DIET' LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewItemEntity>>() {
      @Override
      @NonNull
      public List<ReviewItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "source_type");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfNimaScore = CursorUtil.getColumnIndexOrThrow(_cursor, "nimaScore");
          final int _cursorIndexOfMusiqScore = CursorUtil.getColumnIndexOrThrow(_cursor, "musiqScore");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfExposureScore = CursorUtil.getColumnIndexOrThrow(_cursor, "exposureScore");
          final int _cursorIndexOfAreEyesClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "areEyesClosed");
          final int _cursorIndexOfSmilingProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "smilingProbability");
          final int _cursorIndexOfFaceEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "faceEmbedding");
          final int _cursorIndexOfEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "embedding");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "cluster_id");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final List<ReviewItemEntity> _result = new ArrayList<ReviewItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpSource_type;
            _tmpSource_type = _cursor.getString(_cursorIndexOfSourceType);
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Double _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getDouble(_cursorIndexOfNimaScore);
            }
            final Float _tmpMusiqScore;
            if (_cursor.isNull(_cursorIndexOfMusiqScore)) {
              _tmpMusiqScore = null;
            } else {
              _tmpMusiqScore = _cursor.getFloat(_cursorIndexOfMusiqScore);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final Float _tmpExposureScore;
            if (_cursor.isNull(_cursorIndexOfExposureScore)) {
              _tmpExposureScore = null;
            } else {
              _tmpExposureScore = _cursor.getFloat(_cursorIndexOfExposureScore);
            }
            final Boolean _tmpAreEyesClosed;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp == null ? null : _tmp != 0;
            final Float _tmpSmilingProbability;
            if (_cursor.isNull(_cursorIndexOfSmilingProbability)) {
              _tmpSmilingProbability = null;
            } else {
              _tmpSmilingProbability = _cursor.getFloat(_cursorIndexOfSmilingProbability);
            }
            final byte[] _tmpFaceEmbedding;
            if (_cursor.isNull(_cursorIndexOfFaceEmbedding)) {
              _tmpFaceEmbedding = null;
            } else {
              _tmpFaceEmbedding = _cursor.getBlob(_cursorIndexOfFaceEmbedding);
            }
            final byte[] _tmpEmbedding;
            if (_cursor.isNull(_cursorIndexOfEmbedding)) {
              _tmpEmbedding = null;
            } else {
              _tmpEmbedding = _cursor.getBlob(_cursorIndexOfEmbedding);
            }
            final String _tmpCluster_id;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpCluster_id = null;
            } else {
              _tmpCluster_id = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsUploaded;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_1 != 0;
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            _item = new ReviewItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpSource_type,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpCluster_id,_tmpIsUploaded,_tmpIsSelectedByUser);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object isUriProcessed(final String uri, final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM review_items WHERE uri = ? LIMIT 1)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uri);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getProcessedCountFlow() {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE status = 'KEPT' OR status = 'DELETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"review_items"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAnalyzedItemsWithoutCluster(final String sourceType,
      final Continuation<? super List<ReviewItemEntity>> $completion) {
    final String _sql = "SELECT * FROM review_items WHERE (status = 'ANALYZED' OR status = 'STATUS_REJECTED') AND cluster_id IS NULL AND source_type = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sourceType);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewItemEntity>>() {
      @Override
      @NonNull
      public List<ReviewItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "source_type");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfNimaScore = CursorUtil.getColumnIndexOrThrow(_cursor, "nimaScore");
          final int _cursorIndexOfMusiqScore = CursorUtil.getColumnIndexOrThrow(_cursor, "musiqScore");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfExposureScore = CursorUtil.getColumnIndexOrThrow(_cursor, "exposureScore");
          final int _cursorIndexOfAreEyesClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "areEyesClosed");
          final int _cursorIndexOfSmilingProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "smilingProbability");
          final int _cursorIndexOfFaceEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "faceEmbedding");
          final int _cursorIndexOfEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "embedding");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "cluster_id");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final List<ReviewItemEntity> _result = new ArrayList<ReviewItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpSource_type;
            _tmpSource_type = _cursor.getString(_cursorIndexOfSourceType);
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Double _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getDouble(_cursorIndexOfNimaScore);
            }
            final Float _tmpMusiqScore;
            if (_cursor.isNull(_cursorIndexOfMusiqScore)) {
              _tmpMusiqScore = null;
            } else {
              _tmpMusiqScore = _cursor.getFloat(_cursorIndexOfMusiqScore);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final Float _tmpExposureScore;
            if (_cursor.isNull(_cursorIndexOfExposureScore)) {
              _tmpExposureScore = null;
            } else {
              _tmpExposureScore = _cursor.getFloat(_cursorIndexOfExposureScore);
            }
            final Boolean _tmpAreEyesClosed;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp == null ? null : _tmp != 0;
            final Float _tmpSmilingProbability;
            if (_cursor.isNull(_cursorIndexOfSmilingProbability)) {
              _tmpSmilingProbability = null;
            } else {
              _tmpSmilingProbability = _cursor.getFloat(_cursorIndexOfSmilingProbability);
            }
            final byte[] _tmpFaceEmbedding;
            if (_cursor.isNull(_cursorIndexOfFaceEmbedding)) {
              _tmpFaceEmbedding = null;
            } else {
              _tmpFaceEmbedding = _cursor.getBlob(_cursorIndexOfFaceEmbedding);
            }
            final byte[] _tmpEmbedding;
            if (_cursor.isNull(_cursorIndexOfEmbedding)) {
              _tmpEmbedding = null;
            } else {
              _tmpEmbedding = _cursor.getBlob(_cursorIndexOfEmbedding);
            }
            final String _tmpCluster_id;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpCluster_id = null;
            } else {
              _tmpCluster_id = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsUploaded;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_1 != 0;
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            _item = new ReviewItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpSource_type,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpCluster_id,_tmpIsUploaded,_tmpIsSelectedByUser);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getImagesByDateRange(final long start, final long end,
      final Continuation<? super List<ReviewItemEntity>> $completion) {
    final String _sql = "SELECT * FROM review_items WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewItemEntity>>() {
      @Override
      @NonNull
      public List<ReviewItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "source_type");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfNimaScore = CursorUtil.getColumnIndexOrThrow(_cursor, "nimaScore");
          final int _cursorIndexOfMusiqScore = CursorUtil.getColumnIndexOrThrow(_cursor, "musiqScore");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfExposureScore = CursorUtil.getColumnIndexOrThrow(_cursor, "exposureScore");
          final int _cursorIndexOfAreEyesClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "areEyesClosed");
          final int _cursorIndexOfSmilingProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "smilingProbability");
          final int _cursorIndexOfFaceEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "faceEmbedding");
          final int _cursorIndexOfEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "embedding");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "cluster_id");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final List<ReviewItemEntity> _result = new ArrayList<ReviewItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpSource_type;
            _tmpSource_type = _cursor.getString(_cursorIndexOfSourceType);
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Double _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getDouble(_cursorIndexOfNimaScore);
            }
            final Float _tmpMusiqScore;
            if (_cursor.isNull(_cursorIndexOfMusiqScore)) {
              _tmpMusiqScore = null;
            } else {
              _tmpMusiqScore = _cursor.getFloat(_cursorIndexOfMusiqScore);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final Float _tmpExposureScore;
            if (_cursor.isNull(_cursorIndexOfExposureScore)) {
              _tmpExposureScore = null;
            } else {
              _tmpExposureScore = _cursor.getFloat(_cursorIndexOfExposureScore);
            }
            final Boolean _tmpAreEyesClosed;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp == null ? null : _tmp != 0;
            final Float _tmpSmilingProbability;
            if (_cursor.isNull(_cursorIndexOfSmilingProbability)) {
              _tmpSmilingProbability = null;
            } else {
              _tmpSmilingProbability = _cursor.getFloat(_cursorIndexOfSmilingProbability);
            }
            final byte[] _tmpFaceEmbedding;
            if (_cursor.isNull(_cursorIndexOfFaceEmbedding)) {
              _tmpFaceEmbedding = null;
            } else {
              _tmpFaceEmbedding = _cursor.getBlob(_cursorIndexOfFaceEmbedding);
            }
            final byte[] _tmpEmbedding;
            if (_cursor.isNull(_cursorIndexOfEmbedding)) {
              _tmpEmbedding = null;
            } else {
              _tmpEmbedding = _cursor.getBlob(_cursorIndexOfEmbedding);
            }
            final String _tmpCluster_id;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpCluster_id = null;
            } else {
              _tmpCluster_id = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsUploaded;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_1 != 0;
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            _item = new ReviewItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpSource_type,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpCluster_id,_tmpIsUploaded,_tmpIsSelectedByUser);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getActiveDietCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE source_type = 'DIET' AND status != 'KEPT' AND status != 'DELETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getActiveDietCountFlow() {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE source_type = 'DIET' AND status != 'KEPT' AND status != 'DELETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"review_items"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getClusteredDietCountFlow() {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE source_type = 'DIET' AND status = 'CLUSTERED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"review_items"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getDailyKeptCountFlow(final long startOfDay) {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE status = 'KEPT' AND timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"review_items"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getDailyDeletedCountFlow(final long startOfDay) {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE status = 'DELETED' AND timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"review_items"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getKeptCountByDateRangeFlow(final long start, final long end) {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE status = 'KEPT' AND timestamp >= ? AND timestamp <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"review_items"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getDeletedCountByDateRangeFlow(final long start, final long end) {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE status = 'DELETED' AND timestamp >= ? AND timestamp <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"review_items"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getKeptCountByDateRange(final long start, final long end,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE status = 'KEPT' AND timestamp >= ? AND timestamp <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDeletedCountByDateRange(final long start, final long end,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE status = 'DELETED' AND timestamp >= ? AND timestamp <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMemoryItemCount(final String date,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM review_items WHERE source_type = 'MEMORY' AND strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch', 'localtime')) = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getKeptUnsyncedItems(
      final Continuation<? super List<ReviewItemEntity>> $completion) {
    final String _sql = "SELECT * FROM review_items WHERE status = 'KEPT' AND isUploaded = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewItemEntity>>() {
      @Override
      @NonNull
      public List<ReviewItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "source_type");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfNimaScore = CursorUtil.getColumnIndexOrThrow(_cursor, "nimaScore");
          final int _cursorIndexOfMusiqScore = CursorUtil.getColumnIndexOrThrow(_cursor, "musiqScore");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfExposureScore = CursorUtil.getColumnIndexOrThrow(_cursor, "exposureScore");
          final int _cursorIndexOfAreEyesClosed = CursorUtil.getColumnIndexOrThrow(_cursor, "areEyesClosed");
          final int _cursorIndexOfSmilingProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "smilingProbability");
          final int _cursorIndexOfFaceEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "faceEmbedding");
          final int _cursorIndexOfEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "embedding");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "cluster_id");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final List<ReviewItemEntity> _result = new ArrayList<ReviewItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpSource_type;
            _tmpSource_type = _cursor.getString(_cursorIndexOfSourceType);
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Double _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getDouble(_cursorIndexOfNimaScore);
            }
            final Float _tmpMusiqScore;
            if (_cursor.isNull(_cursorIndexOfMusiqScore)) {
              _tmpMusiqScore = null;
            } else {
              _tmpMusiqScore = _cursor.getFloat(_cursorIndexOfMusiqScore);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final Float _tmpExposureScore;
            if (_cursor.isNull(_cursorIndexOfExposureScore)) {
              _tmpExposureScore = null;
            } else {
              _tmpExposureScore = _cursor.getFloat(_cursorIndexOfExposureScore);
            }
            final Boolean _tmpAreEyesClosed;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp == null ? null : _tmp != 0;
            final Float _tmpSmilingProbability;
            if (_cursor.isNull(_cursorIndexOfSmilingProbability)) {
              _tmpSmilingProbability = null;
            } else {
              _tmpSmilingProbability = _cursor.getFloat(_cursorIndexOfSmilingProbability);
            }
            final byte[] _tmpFaceEmbedding;
            if (_cursor.isNull(_cursorIndexOfFaceEmbedding)) {
              _tmpFaceEmbedding = null;
            } else {
              _tmpFaceEmbedding = _cursor.getBlob(_cursorIndexOfFaceEmbedding);
            }
            final byte[] _tmpEmbedding;
            if (_cursor.isNull(_cursorIndexOfEmbedding)) {
              _tmpEmbedding = null;
            } else {
              _tmpEmbedding = _cursor.getBlob(_cursorIndexOfEmbedding);
            }
            final String _tmpCluster_id;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpCluster_id = null;
            } else {
              _tmpCluster_id = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsUploaded;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_1 != 0;
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            _item = new ReviewItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpSource_type,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpCluster_id,_tmpIsUploaded,_tmpIsSelectedByUser);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllProcessedUris(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT uri FROM review_items";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatusByIds(final List<Long> ids, final String newStatus,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE review_items SET status = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindString(_argIndex, newStatus);
        _argIndex = 2;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateClusterInfo(final String clusterId, final List<Long> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE review_items SET cluster_id = ");
        _stringBuilder.append("?");
        _stringBuilder.append(", status = 'CLUSTERED' WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindString(_argIndex, clusterId);
        _argIndex = 2;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateClusterIdOnly(final String clusterId, final List<Long> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE review_items SET cluster_id = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindString(_argIndex, clusterId);
        _argIndex = 2;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsUploaded(final List<Long> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE review_items SET isUploaded = 1 WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
