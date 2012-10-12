using System;
using System.Security.Permissions;
using Microsoft.Win32;
using System.Collections;

namespace MedCommons
{
	/// <summary>
	/// Summary description for ConnectionConfiguration.
	/// </summary>
	public class ConnectionConfiguration
	{
		private string name;
		private string host;
		private string port;
		private string type;
		private string userName;
		private string password;

		public ConnectionConfiguration(String name, String host, String port, String type, String user, String password)
		{
			this.ConnectionName = name;
			this.Host = host;
			this.Port = port;
			this.Type = type;
			this.UserName = user;
			this.Password = password;
		}

		public string ConnectionName 
		{
			get 
			{
				return name;
			}
			set 
			{
				this.name = value;
			}
		}

		public string Host 
		{
			get 
			{
				return this.host;
			}
			set 
			{
				this.host = value;
			}
		}

		public string Port 
		{
			get 
			{
				return this.port;
			}
			set 
			{
				this.port = value;
			}
		}

		public string Type 
		{
			get 
			{
				return this.type;
			}
			set 
			{
				this.type = value;
			}
		}

		public string UserName {
			get
			{
				return this.userName;
			}
			set
			{
				this.userName = value;
			}
		}

		public string Password {
			get
			{
				return this.password;
			}
			set 
			{
				this.password = value;
			}
		}

		public string ServiceUrl 
		{
			get 
			{
				return "http://" + this.Host + ":"  + this.Port+ "/router/status.do";
			}
		}

		/// <summary>
		/// Saves this connection in the registry
		/// </summary>
		public void save() 
		{
			RegistryPermission rp = new RegistryPermission(PermissionState.Unrestricted);
			rp.Assert();
			RegistryKey key = Registry.CurrentUser.OpenSubKey("Software\\MedCommons\\Connections",true);
			RegistryKey subKey = key.OpenSubKey(ConnectionName,true);
			if(subKey==null) 
			{
				subKey = key.CreateSubKey(this.ConnectionName);
			}
			subKey.SetValue("Host", Host);
			subKey.SetValue("Port", Port);
			subKey.SetValue("User", UserName);
			subKey.SetValue("Password", Password);
		}

		public static ArrayList loadAll() 
		{
			ArrayList connections = new ArrayList(10);
			RegistryKey key = Registry.CurrentUser.OpenSubKey("Software\\MedCommons\\Connections");
			if(key != null) 
			{
				foreach(string subKeyName in key.GetSubKeyNames()) 
				{
					connections.Add(ConnectionConfiguration.load(subKeyName));
				}
			}
			return connections;
		}

		public static ConnectionConfiguration load(string name) 
		{
			RegistryKey key = Registry.CurrentUser.OpenSubKey("Software\\MedCommons\\Connections");
			string host = key.OpenSubKey(name).GetValue("Host").ToString();
			string port = key.OpenSubKey(name).GetValue("Port").ToString();
			object userObj = key.OpenSubKey(name).GetValue("User");

			string user;
			if (userObj == null)
			{
				user = System.Environment.UserName;
			}
			else
				user = userObj.ToString();

			string password = key.OpenSubKey(name).GetValue("Password") == null ? ""
				: key.OpenSubKey(name).GetValue("Password").ToString();

			string type="router";
			if(name == "MedCommons Central")
				type = "central";
			else
				if(name == "Local Computer")
				type = "local";

			return new ConnectionConfiguration(name, host, port, type, user, password);
		}
	}
}
