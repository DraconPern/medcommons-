dataSource {
    pooled = true
  username = "root"
    password = ""
  driverClassName = "com.mysql.jdbc.Driver"
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='com.opensymphony.oscache.hibernate.OSCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
            // dbCreate = "create-drop" // one of 'create', 'create-drop','update'
            // driverClassName = "org.hsqldb.jdbcDriver"
            driverClassName = "com.mysql.jdbc.Driver"
            url = "jdbc:mysql://localhost/mcx"
            logSql = true 
        }
    }
    test {
        dataSource {
            username="root"
            password="Kfm0fH9+oJT2sNNY"
            url = "jdbc:mysql://localhost/mcx"
        }
    }
    
    ci {
        dataSource {
            username="root"
            password="Kfm0fH9+oJT2sNNY"
            url = "jdbc:mysql://localhost/mcx"
        }
    }


    timctest {
        dataSource {
            username="root"
            password="Kfm0fH9+oJT2sNNY"
            url = "jdbc:mysql://localhost/mcx"
        }
    }

    timcqa {
        dataSource {
	        username="medcommons"
	        password=""
	        url = "jdbc:mysql://localhost/mcx"
        }
    }

    qa {
        dataSource {
	        username="medcommons"
	        password=""
	        url = "jdbc:mysql://localhost/mcx"
        }
    }
    
    production {
        dataSource {
	        username="medcommons"
	        password=""
	        url = "jdbc:mysql://localhost/mcx"
        }
    }

    timc {
        dataSource {
	        username="medcommons"
	        password=""
	        url = "jdbc:mysql://localhost/mcx"
        }
    }
}
