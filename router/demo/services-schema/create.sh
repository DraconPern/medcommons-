#!/bin/bash
mysqladmin -u root -f drop mcdb > /dev/null 2>&1
mysqladmin -u root create mcdb

mysql -u root mcdb < 001_Initial_Schema.sql && {
  echo
  echo "Created successfully."
  echo
}
