package net.medcommons.modules.backup;

import java.sql.Timestamp;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class BackupQueueEntry {
	private Long id;
	private Timestamp queuetime;
	private Timestamp starttime;
	private Timestamp endtime;
	private Long accountId;
	private String guid;
	private String status;
	private Long size;
	
	@Override
	public String toString() {
	    return ReflectionToStringBuilder.toString(this);
	}
	
	public void setId(Long id){
		this.id = id;
	}
	public Long getId(){
		return(this.id);
	}
	public void setQueuetime(Timestamp queuetime){
		this.queuetime = queuetime;
	}
	public Timestamp getQueuetime(){
		return(this.queuetime);
	}
	public void setStarttime(Timestamp starttime){
		this.starttime = starttime;
	}
	public Timestamp getStarttime(){
		return(this.starttime);
	}
	public void setEndtime(Timestamp endtime){
		this.endtime = endtime;
	}
	public Timestamp getEndtime(){
		return(this.endtime);
	}
	public void setAccountId(Long accountId){
		this.accountId = accountId;
	}
	public Long getAccountId(){
		return(this.accountId);
	}
	public void setGuid(String guid){
		this.guid = guid;
	}
	public String getGuid(){
		return(this.guid);
	}
	public void setStatus(String status){
		this.status = status;
	}
	public String getStatus(){
		return(this.status);
	}
	public void setSize(Long size){
		this.size = size;
	}
	public Long getSize(){
		return(this.size);
	}
}
/*
 * create table backup_queue (
 `id` bigint(20) NOT NULL auto_increment, 
  `queuetime` timestamp(14) not NULL,
  `starttime` timestamp(14),
  `endtime` timestamp(14),
  `account_id` decimal(16,0) NOT NULL default '0',
  `guid` varchar(64) NOT NULL,
  `status` varchar(255) NOT NULL,
  `size` int(12) NOT NULL default '0',
  primary key (`id`)
) engine=INNODB, comment='queue for backups to offsite location (such as S3)'; 
 */
