/**
 * @copyright
 * ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 * ====================================================================
 * @endcopyright
 *
 * @file SVNClient.h
 * @brief Interface for the SVNClient class
 */

#ifndef SVNCLIENT_H
#define SVNCLIENT_H

#include <vector>
#include <string>
#include <jni.h>
#include "Path.h"

class Revision;
class RevisionRange;
class ClientNotifyCallback;
class ConflictResolverCallback;
class ProgressListener;
class Targets;
class JNIByteArray;
class Prompter;
class CopySources;
class DiffSummaryReceiver;
class BlameCallback;
class ProplistCallback;
class LogMessageCallback;
class InfoCallback;
class ListCallback;
class StatusCallback;
class PatchCallback;
class ChangelistCallback;
class CommitMessage;
class StringArray;
class RevpropTable;
#include "svn_types.h"
#include "svn_client.h"
#include "SVNBase.h"

class SVNClient :public SVNBase
{
 public:
  void patch(const char *patchPath, const char *targetPath, bool dryRun,
             int stripCount, bool reverse, bool ignoreWhitespace,
             bool removeTempfiles, PatchCallback *callback);
  void info2(const char *path, Revision &revision, Revision &pegRevision,
             svn_depth_t depth, StringArray &changelists,
             InfoCallback *callback);
  void unlock(Targets &targets, bool force);
  void lock(Targets &targets, const char *comment, bool force);
  jobject revProperties(const char *path, Revision &revision);
  void cancelOperation();
  void commitMessageHandler(CommitMessage *commitMessage);
  const char *getConfigDirectory();

  /**
   * Set the configuration directory, taking the usual steps to
   * ensure that Subversion's config file templates exist in the
   * specified location.
   */
  void setConfigDirectory(const char *configDir);

  void blame(const char *path, Revision &pegRevision,
             Revision &revisionStart, Revision &revisionEnd,
             bool ignoreMimeType, bool includeMergedRevisions,
             BlameCallback *callback);
  void relocate(const char *from, const char *to, const char *path,
                bool recurse);
  jbyteArray fileContent(const char *path, Revision &revision,
                         Revision &pegRevision);
  void streamFileContent(const char *path, Revision &revision,
                         Revision &pegRevision, jobject outputStream,
                         size_t bufSize);
  void propertySet(const char *path, const char *name, const char *value,
                   svn_depth_t depth, StringArray &changelists, bool force,
                   RevpropTable &revprops);
  void properties(const char *path, Revision &revision,
                  Revision &pegRevision, svn_depth_t depth,
                  StringArray &changelists, ProplistCallback *callback);
  jobject getMergeinfo(const char *target, Revision &pegRevision);
  void getMergeinfoLog(int type, const char *pathOrURL,
                       Revision &pegRevision, const char *mergeSourceURL,
                       Revision &srcPegRevision, bool discoverChangedPaths,
                       svn_depth_t depth, StringArray &revProps,
                       LogMessageCallback *callback);
  jobject suggestMergeSources(const char *path, Revision &pegRevision);
  void merge(const char *path1, Revision &revision1, const char *path2,
             Revision &revision2, const char *localPath, bool force,
             svn_depth_t depth, bool ignoreAncestry, bool dryRun,
             bool recordOnly);
  void merge(const char *path, Revision &pegRevision,
             std::vector<RevisionRange> &rangesToMerge,
             const char *localPath, bool force, svn_depth_t depth,
             bool ignoreAncestry, bool dryRun, bool recordOnly);
  void mergeReintegrate(const char *path, Revision &pegRevision,
                        const char *localPath, bool dryRun);
  void doImport(const char *path, const char *url, const char *message,
                svn_depth_t depth, bool noIgnore, bool ignoreUnknownNodeTypes,
                RevpropTable &revprops);
  jlong doSwitch(const char *path, const char *url, Revision &revision,
                 Revision &pegRevision, svn_depth_t depth,
                 bool depthIsSticky, bool ignoreExternals,
                 bool allowUnverObstructions);
  jlong doExport(const char *srcPath, const char *destPath,
                 Revision &revision, Revision &pegRevision, bool force,
                 bool ignoreExternals, svn_depth_t depth,
                 const char *nativeEOL);
  void resolve(const char *path, svn_depth_t depth,
               svn_wc_conflict_choice_t choice);
  void cleanup(const char *path);
  void mkdir(Targets &targets, const char *message, bool makeParents,
             RevpropTable &revprops);
  void move(Targets &srcPaths, const char *destPath,
            const char *message, bool force, bool moveAsChild,
            bool makeParents, RevpropTable &revprops);
  void copy(CopySources &copySources, const char *destPath,
            const char *message, bool copyAsChild, bool makeParents,
            bool ignoreExternals, RevpropTable &revprops);
  jlong commit(Targets &targets, const char *message, svn_depth_t depth,
               bool noUnlock, bool keepChangelist,
               StringArray &changelists, RevpropTable &revprops);
  jlongArray update(Targets &targets, Revision &revision, svn_depth_t depth,
                    bool depthIsSticky, bool ignoreExternals,
                    bool allowUnverObstructions);
  void add(const char *path, svn_depth_t depth, bool force, bool no_ignore,
           bool add_parents);
  void revert(const char *path, svn_depth_t depth, StringArray &changelists);
  void remove(Targets &targets, const char *message, bool force,
              bool keep_local, RevpropTable &revprops);
  void notification2(ClientNotifyCallback *notify2);
  void setConflictResolver(ConflictResolverCallback *conflictResolver);
  void setProgressListener(ProgressListener *progressListener);
  jlong checkout(const char *moduleName, const char *destPath,
                 Revision &revision, Revision &pegRevsion, svn_depth_t depth,
                 bool ignoreExternals, bool allowUnverObstructions);
  void logMessages(const char *path, Revision &pegRevision,
                   std::vector<RevisionRange> &ranges, bool stopOnCopy,
                   bool discoverPaths, bool includeMergedRevisions,
                   StringArray &revProps,
                   long limit, LogMessageCallback *callback);
  void setPrompt(Prompter *prompter);
  void password(const char *pi_password);
  void username(const char *pi_username);
  jstring getAdminDirectoryName();
  jboolean isAdminDirectory(const char *name);
  void addToChangelist(Targets &srcPaths, const char *changelist,
                       svn_depth_t depth, StringArray &changelists);
  void removeFromChangelists(Targets &srcPaths, svn_depth_t depth,
                             StringArray &changelists);
  void getChangelists(const char *path, StringArray &changelists,
                      svn_depth_t depth, ChangelistCallback *callback);
  void status(const char *path, svn_depth_t depth, bool onServer,
              bool getAll, bool noIgnore, bool ignoreExternals,
              StringArray &changelists, StatusCallback *callback);
  void list(const char *url, Revision &revision, Revision &pegRevision,
            svn_depth_t depth, int direntFields, bool fetchLocks,
            ListCallback *callback);
  jbyteArray revProperty(const char *path, const char *name, Revision &rev);
  void setRevProperty(const char *path, const char *name,
                      Revision &rev, const char *value,
                      const char *original_value, bool force);
  jstring getVersionInfo(const char *path, const char *trailUrl,
                         bool lastChanged);
  void upgrade(const char *path);
  jbyteArray propertyGet(const char *path, const char *name,
                         Revision &revision, Revision &pegRevision);
  void diff(const char *target1, Revision &revision1,
            const char *target2, Revision &revision2,
            const char *relativeToDir, const char *outfileName,
            svn_depth_t depth, StringArray &changelists,
            bool ignoreAncestry, bool noDiffDelete, bool force,
            bool showCopiesAsAdds);
  void diff(const char *target, Revision &pegevision,
            Revision &startRevision, Revision &endRevision,
            const char *relativeToDir, const char *outfileName,
            svn_depth_t depth, StringArray &changelists,
            bool ignoreAncestry, bool noDiffDelete, bool force,
            bool showCopiesAsAdds);
  void diffSummarize(const char *target1, Revision &revision1,
                     const char *target2, Revision &revision2,
                     svn_depth_t depth, StringArray &changelists,
                     bool ignoreAncestry, DiffSummaryReceiver &receiver);
  void diffSummarize(const char *target, Revision &pegRevision,
                     Revision &startRevision, Revision &endRevision,
                     svn_depth_t depth, StringArray &changelists,
                     bool ignoreAncestry, DiffSummaryReceiver &receiver);

  const char *getLastPath();
  void dispose(jobject jthis);
  static SVNClient *getCppObject(jobject jthis);
  SVNClient();
  virtual ~SVNClient();
 private:
  static svn_error_t *checkCancel(void *cancelBaton);
  svn_client_ctx_t *getContext(const char *message);
  svn_stream_t *createReadStream(apr_pool_t *pool, const char *path,
                                 Revision &revision, Revision &pegRevision,
                                 size_t &size);
  /**
   * Shared implementation for diff() APIs. When pegRevision is
   * provided, revision1 and revision2 equate to startRevision and
   * endRevision (respectively), and target2 is ignored.
   */
  void diff(const char *target1, Revision &revision1,
            const char *target2, Revision &revision2,
            Revision *pegRevision, const char *relativeToDir,
            const char *outfileName, svn_depth_t depth, StringArray &changelists,
            bool ignoreAncestry, bool noDiffDelete, bool force,
            bool showCopiesAsAdds);

  ClientNotifyCallback *m_notify2;
  ConflictResolverCallback *m_conflictResolver;
  ProgressListener *m_progressListener;
  Prompter *m_prompter;
  Path m_lastPath;
  bool m_cancelOperation;
  CommitMessage *m_commitMessage;

  /**
   * Implements the svn_client_get_commit_log3_t API.
   */
  static svn_error_t *getCommitMessage(const char **log_msg,
                                       const char **tmp_file,
                                       const apr_array_header_t *
                                       commit_items,
                                       void *baton,
                                       apr_pool_t *pool);
  /**
   * Produce a baton for the getCommitMessage() callback.
   */
  void *getCommitMessageBaton(const char *message);

  std::string m_userName;
  std::string m_passWord;
  std::string m_configDir;
};

#endif // SVNCLIENT_H