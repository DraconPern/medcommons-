#!groovy
import org.jivesoftware.smack.*

println "Smack Test Program"

def config = new ConnectionConfiguration('talk.google.com', 5222, 'gmail.com')

XMPPConnection connection = new XMPPConnection(config);
println "Connecting ..."
connection.connect()
println "Logging in ..."
connection.login("onemctest@gmail.com", "medcommons");
println "Sending chat ..."
def messageText = System.in.text
args.each {
  Chat chat = connection.chatManager.createChat(it, null);
  chat.sendMessage(messageText);
}
