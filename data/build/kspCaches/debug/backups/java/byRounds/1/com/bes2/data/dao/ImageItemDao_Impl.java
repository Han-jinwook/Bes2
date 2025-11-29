package com.bes2.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.bes2.data.model.ImageItemEntity;
import com.bes2.data.model.StatusCount;
import java.lang.Boolean;
import java.lang.Class;
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
public final class ImageItemDao_Impl implements ImageItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ImageItemEntity> __insertionAdapterOfImageItemEntity;

  private final EntityDeletionOrUpdateAdapter<ImageItemEntity> __deletionAdapterOfImageItemEntity;

  private final EntityDeletionOrUpdateAdapter<ImageItemEntity> __updateAdapterOfImageItemEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateImageItemStatus;

  private final SharedSQLiteStatement __preparedStmtOfClearAllImageItems;

  public ImageItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfImageItemEntity = new EntityInsertionAdapter<ImageItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `image_items` (`id`,`uri`,`filePath`,`timestamp`,`status`,`pHash`,`nimaScore`,`musiqScore`,`blurScore`,`exposureScore`,`areEyesClosed`,`smilingProbability`,`faceEmbedding`,`embedding`,`cluster_id`,`isSelectedByUser`,`isUploaded`,`isBestInCluster`,`category`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUri());
        statement.bindString(3, entity.getFilePath());
        statement.bindLong(4, entity.getTimestamp());
        statement.bindString(5, entity.getStatus());
        if (entity.getPHash() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPHash());
        }
        if (entity.getNimaScore() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getNimaScore());
        }
        if (entity.getMusiqScore() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getMusiqScore());
        }
        if (entity.getBlurScore() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getBlurScore());
        }
        if (entity.getExposureScore() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getExposureScore());
        }
        final Integer _tmp = entity.getAreEyesClosed() == null ? null : (entity.getAreEyesClosed() ? 1 : 0);
        if (_tmp == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, _tmp);
        }
        if (entity.getSmilingProbability() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getSmilingProbability());
        }
        if (entity.getFaceEmbedding() == null) {
          statement.bindNull(13);
        } else {
          statement.bindBlob(13, entity.getFaceEmbedding());
        }
        if (entity.getEmbedding() == null) {
          statement.bindNull(14);
        } else {
          statement.bindBlob(14, entity.getEmbedding());
        }
        if (entity.getClusterId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getClusterId());
        }
        final int _tmp_1 = entity.isSelectedByUser() ? 1 : 0;
        statement.bindLong(16, _tmp_1);
        final int _tmp_2 = entity.isUploaded() ? 1 : 0;
        statement.bindLong(17, _tmp_2);
        final int _tmp_3 = entity.isBestInCluster() ? 1 : 0;
        statement.bindLong(18, _tmp_3);
        if (entity.getCategory() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCategory());
        }
      }
    };
    this.__deletionAdapterOfImageItemEntity = new EntityDeletionOrUpdateAdapter<ImageItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `image_items` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageItemEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfImageItemEntity = new EntityDeletionOrUpdateAdapter<ImageItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `image_items` SET `id` = ?,`uri` = ?,`filePath` = ?,`timestamp` = ?,`status` = ?,`pHash` = ?,`nimaScore` = ?,`musiqScore` = ?,`blurScore` = ?,`exposureScore` = ?,`areEyesClosed` = ?,`smilingProbability` = ?,`faceEmbedding` = ?,`embedding` = ?,`cluster_id` = ?,`isSelectedByUser` = ?,`isUploaded` = ?,`isBestInCluster` = ?,`category` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUri());
        statement.bindString(3, entity.getFilePath());
        statement.bindLong(4, entity.getTimestamp());
        statement.bindString(5, entity.getStatus());
        if (entity.getPHash() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPHash());
        }
        if (entity.getNimaScore() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getNimaScore());
        }
        if (entity.getMusiqScore() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getMusiqScore());
        }
        if (entity.getBlurScore() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getBlurScore());
        }
        if (entity.getExposureScore() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getExposureScore());
        }
        final Integer _tmp = entity.getAreEyesClosed() == null ? null : (entity.getAreEyesClosed() ? 1 : 0);
        if (_tmp == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, _tmp);
        }
        if (entity.getSmilingProbability() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getSmilingProbability());
        }
        if (entity.getFaceEmbedding() == null) {
          statement.bindNull(13);
        } else {
          statement.bindBlob(13, entity.getFaceEmbedding());
        }
        if (entity.getEmbedding() == null) {
          statement.bindNull(14);
        } else {
          statement.bindBlob(14, entity.getEmbedding());
        }
        if (entity.getClusterId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getClusterId());
        }
        final int _tmp_1 = entity.isSelectedByUser() ? 1 : 0;
        statement.bindLong(16, _tmp_1);
        final int _tmp_2 = entity.isUploaded() ? 1 : 0;
        statement.bindLong(17, _tmp_2);
        final int _tmp_3 = entity.isBestInCluster() ? 1 : 0;
        statement.bindLong(18, _tmp_3);
        if (entity.getCategory() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCategory());
        }
        statement.bindLong(20, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateImageItemStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE image_items SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllImageItems = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM image_items";
        return _query;
      }
    };
  }

  @Override
  public Object insertImageItem(final ImageItemEntity imageItem,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfImageItemEntity.insertAndReturnId(imageItem);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertImageItems(final List<ImageItemEntity> imageItems,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfImageItemEntity.insertAndReturnIdsList(imageItems);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteImageItem(final ImageItemEntity imageItem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfImageItemEntity.handle(imageItem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateImageItem(final ImageItemEntity imageItem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfImageItemEntity.handle(imageItem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateImageItemStatus(final long id, final String newStatus,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateImageItemStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, newStatus);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateImageItemStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllImageItems(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllImageItems.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllImageItems.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<ImageItemEntity> getImageItemById(final long id) {
    final String _sql = "SELECT * FROM image_items WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_items"}, new Callable<ImageItemEntity>() {
      @Override
      @Nullable
      public ImageItemEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final ImageItemEntity _result;
          if (_cursor.moveToFirst()) {
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _result = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
          } else {
            _result = null;
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
  public Object getImagesByIds(final List<Long> ids,
      final Continuation<? super List<ImageItemEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM image_items WHERE id IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (long _item : ids) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item_1;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item_1 = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
            _result.add(_item_1);
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
  public Flow<List<ImageItemEntity>> getAllImageItems() {
    final String _sql = "SELECT * FROM image_items ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_items"}, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Object getAllImageItemsList(
      final Continuation<? super List<ImageItemEntity>> $completion) {
    final String _sql = "SELECT * FROM image_items ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Flow<List<ImageItemEntity>> getImageItemsByStatusFlow(final String status) {
    final String _sql = "SELECT * FROM image_items WHERE status = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, status);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_items"}, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Object getImageItemsListByStatus(final String status,
      final Continuation<? super List<ImageItemEntity>> $completion) {
    final String _sql = "SELECT * FROM image_items WHERE status = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, status);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Object getImagesByStatusAndUploadFlag(final String status, final boolean isUploaded,
      final Continuation<? super List<ImageItemEntity>> $completion) {
    final String _sql = "SELECT * FROM image_items WHERE status = ? AND isUploaded = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, status);
    _argIndex = 2;
    final int _tmp = isUploaded ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final Integer _tmp_1;
            if (_cursor.isNull(_cursorIndexOfAreEyesClosed)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getInt(_cursorIndexOfAreEyesClosed);
            }
            _tmpAreEyesClosed = _tmp_1 == null ? null : _tmp_1 != 0;
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_2 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_3 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_4 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Object getAnalyzedImagesWithoutCluster(
      final Continuation<? super List<ImageItemEntity>> $completion) {
    final String _sql = "SELECT * FROM image_items WHERE status = 'ANALYZED' AND cluster_id IS NULL ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Flow<List<ImageItemEntity>> getImageItemsByClusterId(final String clusterId) {
    final String _sql = "SELECT * FROM image_items WHERE cluster_id = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, clusterId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_items"}, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Object isUriProcessed(final String uri, final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM image_items WHERE uri = ? LIMIT 1)";
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
  public Object getKeptAndNotUploadedImages(
      final Continuation<? super List<ImageItemEntity>> $completion) {
    final String _sql = "SELECT * FROM image_items WHERE status = 'KEPT' AND isUploaded = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Flow<List<StatusCount>> getDailyStatsFlow(final long startTime) {
    final String _sql = "SELECT status, COUNT(*) as count FROM image_items WHERE timestamp >= ? GROUP BY status";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_items"}, new Callable<List<StatusCount>>() {
      @Override
      @NonNull
      public List<StatusCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfStatus = 0;
          final int _cursorIndexOfCount = 1;
          final List<StatusCount> _result = new ArrayList<StatusCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StatusCount _item;
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new StatusCount(_tmpStatus,_tmpCount);
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
  public Object getStatsByDateRange(final long startTime, final long endTime,
      final Continuation<? super List<StatusCount>> $completion) {
    final String _sql = "SELECT status, COUNT(*) as count FROM image_items WHERE timestamp >= ? AND timestamp <= ? GROUP BY status";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StatusCount>>() {
      @Override
      @NonNull
      public List<StatusCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfStatus = 0;
          final int _cursorIndexOfCount = 1;
          final List<StatusCount> _result = new ArrayList<StatusCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StatusCount _item;
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new StatusCount(_tmpStatus,_tmpCount);
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
  public Object getImageStatusByUri(final String uri,
      final Continuation<? super String> $completion) {
    final String _sql = "SELECT status FROM image_items WHERE uri = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uri);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<String>() {
      @Override
      @Nullable
      public String call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final String _result;
          if (_cursor.moveToFirst()) {
            if (_cursor.isNull(0)) {
              _result = null;
            } else {
              _result = _cursor.getString(0);
            }
          } else {
            _result = null;
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
  public Object countImagesByStatus(final String status,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM image_items WHERE status = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, status);
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
  public Object getImageItemsByCategory(final String category,
      final Continuation<? super List<ImageItemEntity>> $completion) {
    final String _sql = "SELECT * FROM image_items WHERE category = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Object countUnprocessedImagesByCategory(final String category,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM image_items WHERE category = ? AND status != 'KEPT' AND status != 'DELETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
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
  public Object getImagesByDateRange(final long startTime, final long endTime,
      final Continuation<? super List<ImageItemEntity>> $completion) {
    final String _sql = "SELECT * FROM image_items WHERE timestamp >= ? AND timestamp <= ? AND (category != 'DOCUMENT' OR category IS NULL) ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageItemEntity>>() {
      @Override
      @NonNull
      public List<ImageItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
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
          final int _cursorIndexOfIsSelectedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isSelectedByUser");
          final int _cursorIndexOfIsUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isUploaded");
          final int _cursorIndexOfIsBestInCluster = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestInCluster");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<ImageItemEntity> _result = new ArrayList<ImageItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageItemEntity _item;
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
            final String _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getString(_cursorIndexOfPHash);
            }
            final Float _tmpNimaScore;
            if (_cursor.isNull(_cursorIndexOfNimaScore)) {
              _tmpNimaScore = null;
            } else {
              _tmpNimaScore = _cursor.getFloat(_cursorIndexOfNimaScore);
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
            final String _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getString(_cursorIndexOfClusterId);
            }
            final boolean _tmpIsSelectedByUser;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSelectedByUser);
            _tmpIsSelectedByUser = _tmp_1 != 0;
            final boolean _tmpIsUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsUploaded);
            _tmpIsUploaded = _tmp_2 != 0;
            final boolean _tmpIsBestInCluster;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBestInCluster);
            _tmpIsBestInCluster = _tmp_3 != 0;
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            _item = new ImageItemEntity(_tmpId,_tmpUri,_tmpFilePath,_tmpTimestamp,_tmpStatus,_tmpPHash,_tmpNimaScore,_tmpMusiqScore,_tmpBlurScore,_tmpExposureScore,_tmpAreEyesClosed,_tmpSmilingProbability,_tmpFaceEmbedding,_tmpEmbedding,_tmpClusterId,_tmpIsSelectedByUser,_tmpIsUploaded,_tmpIsBestInCluster,_tmpCategory);
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
  public Flow<Integer> getTotalImageCountFlow() {
    final String _sql = "SELECT COUNT(*) FROM image_items";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_items"}, new Callable<Integer>() {
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
  public Flow<Integer> getIndexedImageCountFlow() {
    final String _sql = "SELECT COUNT(*) FROM image_items WHERE embedding IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"image_items"}, new Callable<Integer>() {
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
  public Object updateImageClusterInfo(final String clusterId, final List<Long> imageIds,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE image_items SET status = 'CLUSTERED', cluster_id = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE id IN (");
        final int _inputSize = imageIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindString(_argIndex, clusterId);
        _argIndex = 2;
        for (long _item : imageIds) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          final Integer _result = _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setClusterIdForImages(final String clusterId, final List<Long> imageIds,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE image_items SET cluster_id = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE id IN (");
        final int _inputSize = imageIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindString(_argIndex, clusterId);
        _argIndex = 2;
        for (long _item : imageIds) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          final Integer _result = _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateImageStatusesByIds(final List<Long> ids, final String newStatus,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE image_items SET status = ");
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
          final Integer _result = _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateIsSelectedByIds(final List<Long> ids,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE image_items SET isSelectedByUser = 1 WHERE id IN (");
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
          final Integer _result = _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteImageItemsByIds(final List<Long> ids,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM image_items WHERE id IN (");
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
          final Integer _result = _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateUploadedStatusByUris(final List<String> uris, final boolean uploaded,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE image_items SET isUploaded = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE uri IN (");
        final int _inputSize = uris.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        final int _tmp = uploaded ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        for (String _item : uris) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          final Integer _result = _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateClusterIdByUris(final List<String> uris, final String clusterId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE image_items SET cluster_id = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE uri IN (");
        final int _inputSize = uris.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindString(_argIndex, clusterId);
        _argIndex = 2;
        for (String _item : uris) {
          _stmt.bindString(_argIndex, _item);
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
  public Object updateImageStatusesByUris(final List<String> uris, final String newStatus,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE image_items SET status = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE uri IN (");
        final int _inputSize = uris.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindString(_argIndex, newStatus);
        _argIndex = 2;
        for (String _item : uris) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          final Integer _result = _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return _result;
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
