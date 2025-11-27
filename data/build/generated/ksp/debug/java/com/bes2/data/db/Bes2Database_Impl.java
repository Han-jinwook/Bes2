package com.bes2.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.bes2.data.dao.ImageClusterDao;
import com.bes2.data.dao.ImageClusterDao_Impl;
import com.bes2.data.dao.ImageItemDao;
import com.bes2.data.dao.ImageItemDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class Bes2Database_Impl extends Bes2Database {
  private volatile ImageItemDao _imageItemDao;

  private volatile ImageClusterDao _imageClusterDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `image_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uri` TEXT NOT NULL, `filePath` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `status` TEXT NOT NULL, `pHash` TEXT, `nimaScore` REAL, `musiqScore` REAL, `blurScore` REAL, `exposureScore` REAL, `areEyesClosed` INTEGER, `smilingProbability` REAL, `faceEmbedding` BLOB, `embedding` BLOB, `cluster_id` TEXT, `isSelectedByUser` INTEGER NOT NULL, `isUploaded` INTEGER NOT NULL, `isBestInCluster` INTEGER NOT NULL, `category` TEXT)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_image_items_uri` ON `image_items` (`uri`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `image_clusters` (`id` TEXT NOT NULL, `best_image_uri` TEXT, `second_best_image_uri` TEXT, `creation_time` INTEGER NOT NULL, `review_status` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0d2e3df2e6bcc0eea03854a46beaa6c1')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `image_items`");
        db.execSQL("DROP TABLE IF EXISTS `image_clusters`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsImageItems = new HashMap<String, TableInfo.Column>(19);
        _columnsImageItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("uri", new TableInfo.Column("uri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("filePath", new TableInfo.Column("filePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("pHash", new TableInfo.Column("pHash", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("nimaScore", new TableInfo.Column("nimaScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("musiqScore", new TableInfo.Column("musiqScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("blurScore", new TableInfo.Column("blurScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("exposureScore", new TableInfo.Column("exposureScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("areEyesClosed", new TableInfo.Column("areEyesClosed", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("smilingProbability", new TableInfo.Column("smilingProbability", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("faceEmbedding", new TableInfo.Column("faceEmbedding", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("embedding", new TableInfo.Column("embedding", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("cluster_id", new TableInfo.Column("cluster_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("isSelectedByUser", new TableInfo.Column("isSelectedByUser", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("isUploaded", new TableInfo.Column("isUploaded", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("isBestInCluster", new TableInfo.Column("isBestInCluster", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageItems.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysImageItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesImageItems = new HashSet<TableInfo.Index>(1);
        _indicesImageItems.add(new TableInfo.Index("index_image_items_uri", true, Arrays.asList("uri"), Arrays.asList("ASC")));
        final TableInfo _infoImageItems = new TableInfo("image_items", _columnsImageItems, _foreignKeysImageItems, _indicesImageItems);
        final TableInfo _existingImageItems = TableInfo.read(db, "image_items");
        if (!_infoImageItems.equals(_existingImageItems)) {
          return new RoomOpenHelper.ValidationResult(false, "image_items(com.bes2.data.model.ImageItemEntity).\n"
                  + " Expected:\n" + _infoImageItems + "\n"
                  + " Found:\n" + _existingImageItems);
        }
        final HashMap<String, TableInfo.Column> _columnsImageClusters = new HashMap<String, TableInfo.Column>(5);
        _columnsImageClusters.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageClusters.put("best_image_uri", new TableInfo.Column("best_image_uri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageClusters.put("second_best_image_uri", new TableInfo.Column("second_best_image_uri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageClusters.put("creation_time", new TableInfo.Column("creation_time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageClusters.put("review_status", new TableInfo.Column("review_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysImageClusters = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesImageClusters = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoImageClusters = new TableInfo("image_clusters", _columnsImageClusters, _foreignKeysImageClusters, _indicesImageClusters);
        final TableInfo _existingImageClusters = TableInfo.read(db, "image_clusters");
        if (!_infoImageClusters.equals(_existingImageClusters)) {
          return new RoomOpenHelper.ValidationResult(false, "image_clusters(com.bes2.data.model.ImageClusterEntity).\n"
                  + " Expected:\n" + _infoImageClusters + "\n"
                  + " Found:\n" + _existingImageClusters);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "0d2e3df2e6bcc0eea03854a46beaa6c1", "888f96f17575817e91772ef9ab9a8273");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "image_items","image_clusters");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `image_items`");
      _db.execSQL("DELETE FROM `image_clusters`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ImageItemDao.class, ImageItemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ImageClusterDao.class, ImageClusterDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ImageItemDao imageItemDao() {
    if (_imageItemDao != null) {
      return _imageItemDao;
    } else {
      synchronized(this) {
        if(_imageItemDao == null) {
          _imageItemDao = new ImageItemDao_Impl(this);
        }
        return _imageItemDao;
      }
    }
  }

  @Override
  public ImageClusterDao imageClusterDao() {
    if (_imageClusterDao != null) {
      return _imageClusterDao;
    } else {
      synchronized(this) {
        if(_imageClusterDao == null) {
          _imageClusterDao = new ImageClusterDao_Impl(this);
        }
        return _imageClusterDao;
      }
    }
  }
}
