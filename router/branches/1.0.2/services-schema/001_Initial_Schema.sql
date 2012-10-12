CREATE TABLE DocumentLocation (
  Nodes_NodeID BIGINT NOT NULL,
  Documents_ID INTEGER UNSIGNED NOT NULL,
  ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  IntegrityCheck TIMESTAMP NULL,
  IntegrityStatus INTEGER UNSIGNED NULL,
  EncryptedKey VARCHAR(64) NULL,
  CopyNumber INTEGER UNSIGNED NULL,
  PRIMARY KEY(Nodes_NodeID, Documents_ID, ID),
  INDEX DocumentLocation_FKIndex2(Nodes_NodeID)
);

CREATE TABLE Documents (
  ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  GUID VARCHAR(64) NOT NULL,
  EncryptedKey VARCHAR(64) NULL,
  CreationTime TIMESTAMP NULL,
  RightsTime TIMESTAMP NULL,
  EncryptedHash VARCHAR(64) NULL,
  PRIMARY KEY(ID)
);

CREATE TABLE GroupNode (
  Nodes_NodeID BIGINT NOT NULL,
  Groups_GroupNumber INTEGER NOT NULL,
  PRIMARY KEY(Nodes_NodeID, Groups_GroupNumber),
  INDEX GroupNode_FKIndex1(Nodes_NodeID),
  INDEX GroupNode_FKIndex2(Groups_GroupNumber)
);

CREATE TABLE Groups (
  GroupNumber INTEGER NOT NULL,
  Name VARCHAR(64) NULL,
  Location VARCHAR(64) NULL,
  GroupType VARCHAR(32) NULL,
  AdminID VARCHAR(32) NULL,
  PointOfContanctID VARCHAR(32) NULL,
  PRIMARY KEY(GroupNumber)
);

CREATE TABLE Inbox (
  InboxID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  InboxName VARCHAR(45) NULL,
  InBoxType INTEGER UNSIGNED NULL,
  InBoxLocation VARCHAR(200) NULL,
  PRIMARY KEY(InboxID)
);

CREATE TABLE Inboxes (
  Groups_GroupNumber INTEGER NOT NULL,
  Users_MedCommonsUserID VARCHAR(32) NOT NULL,
  Inbox_InboxID INTEGER UNSIGNED NOT NULL,
  Descriptor VARCHAR(128) NULL,
  DescriptorType INTEGER UNSIGNED NULL,
  Authentication INTEGER UNSIGNED NULL,
  INDEX GroupInbox_FKIndex1(Inbox_InboxID),
  INDEX Usernbox_FKIndex2(Users_MedCommonsUserID),
  INDEX Inboxes_FKIndex3(Groups_GroupNumber)
);

CREATE TABLE NodeRights (
  Nodes_NodeID BIGINT NOT NULL,
  Groups_GroupNumber INTEGER NOT NULL,
  DataRight VARCHAR(32) NULL,
  PRIMARY KEY(Nodes_NodeID, Groups_GroupNumber),
  INDEX NodeRights_FKIndex1(Nodes_NodeID),
  INDEX NodeRights_FKIndex2(Groups_GroupNumber)
);

CREATE TABLE Nodes (
  NodeID BIGINT NOT NULL,
  AdminID VARCHAR(32) NULL,
  eKey BIGINT NULL,
  mKey BIGINT NULL,
  DisplayName VARCHAR(64) NULL,
  Hostname VARCHAR(64) NULL,
  FixedIP INTEGER NULL,
  NodeType INTEGER NULL,
  CreationTime TIMESTAMP NULL,
  LoggingServer VARCHAR(128) NULL,
  PRIMARY KEY(NodeID)
);

CREATE TABLE Rights (
  RightsID INTEGER UNSIGNED NOT NULL,
  Documents_ID INTEGER UNSIGNED NOT NULL,
  Users_MedCommonsUserID VARCHAR(32) NOT NULL,
  Groups_GroupNumber INTEGER NOT NULL,
  Rights VARCHAR(32) NOT NULL,
  CreationTime TIMESTAMP NULL,
  ExpirationTime TIMESTAMP NULL,
  RightsTime TIMESTAMP NULL,
  AcceptedTime TIMESTAMP NULL,
  ID INTEGER UNSIGNED NULL,
  PRIMARY KEY(RightsID),
  INDEX Rights_FKIndex3(Groups_GroupNumber),
  INDEX Rights_FKIndex2(Users_MedCommonsUserID)
);

CREATE TABLE TrackingNumber (
  TrackingNumber VARCHAR(64) NOT NULL,
  Rights_RightsID INTEGER UNSIGNED NOT NULL,
  EncryptedPIN VARCHAR(64) NULL,
  PRIMARY KEY(TrackingNumber),
  INDEX TrackingNumber_FKIndex1(Rights_RightsID)
);

CREATE TABLE UserGroup (
  Users_MedCommonsUserID VARCHAR(32) NOT NULL,
  Groups_GroupNumber INTEGER NOT NULL,
  UserRoleWithGroup VARCHAR(32) NOT NULL,
  AddedByID VARCHAR(32) NULL,
  PRIMARY KEY(Users_MedCommonsUserID, Groups_GroupNumber),
  INDEX UserGroup_FKIndex1(Users_MedCommonsUserID),
  INDEX UserGroup_FKIndex2(Groups_GroupNumber)
);

CREATE TABLE Users (
  MedCommonsUserID VARCHAR(32) NOT NULL,
  TelephoneNumber VARCHAR(64) NULL,
  EmailAddress VARCHAR(64) NULL,
  Credential BLOB NULL,
  CreationTime TIMESTAMP NULL,
  LastAccessTime TIMESTAMP NULL,
  UIRole INTEGER NULL,
  PublicKey VARCHAR(255) NULL,
  PRIMARY KEY(MedCommonsUserID)
);

CREATE TABLE WorklistQueue (
  WorklistID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  Groups_GroupNumber INTEGER NOT NULL,
  Users_MedCommonsUserID VARCHAR(32) NOT NULL,
  Comment_2 VARCHAR(32) NULL,
  PRIMARY KEY(WorklistID),
  INDEX WorklistQueue_FKIndex1(Users_MedCommonsUserID),
  INDEX WorklistQueue_FKIndex2(Groups_GroupNumber)
);

CREATE TABLE WorklistQueueItem (
  WorklistQueue_WorklistID INTEGER UNSIGNED NOT NULL,
  Rights_RightsID INTEGER UNSIGNED NOT NULL,
  PlacedInQueue TIMESTAMP NULL,
  OrderNumber INTEGER UNSIGNED NULL,
  Priority INTEGER UNSIGNED NULL,
  PRIMARY KEY(WorklistQueue_WorklistID, Rights_RightsID),
  INDEX WorklistQueueItem_FKIndex1(WorklistQueue_WorklistID),
  INDEX WorklistQueueItem_FKIndex2(Rights_RightsID)
);

