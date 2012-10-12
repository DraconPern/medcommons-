/*
 * $Id: RecipeServiceClient.java 60 2004-05-06 18:28:34Z mquigley $
 */

package net.medcommons.central.ws.recipe;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

import net.medcommons.recipe.RecipeInvocation;
import net.medcommons.recipe.RecipeInvocationResult;

public class RecipeServiceClient {
  public static void main(String [] args) {
    try {
      String endpoint = "http://medcommons.net:8080/jboss-net/services/RecipeService";
      if(args.length > 0 && "local".equals(args[0])) {
        endpoint = "http://localhost:8080/jboss-net/services/RecipeService";
      }

      Service service = new Service();
      Call call = (Call) service.createCall();

      call.setTargetEndpointAddress(new java.net.URL(endpoint));
      call.setOperationName("submitRecipeInvocation");
      call.addParameter("ri", new QName("", "RecipeInvocation"), RecipeInvocation.class, ParameterMode.IN);      
      call.setReturnType(new QName("", "RecipeInvocationResult"));

      QName qn = new QName("", "RecipeInvocation");
      call.registerTypeMapping(RecipeInvocation.class, qn,
                               new BeanSerializerFactory(RecipeInvocation.class, qn),
                               new BeanDeserializerFactory(RecipeInvocation.class, qn));
      qn = new QName("", "RecipeInvocationResult");
      call.registerTypeMapping(RecipeInvocationResult.class, qn,
                               new BeanSerializerFactory(RecipeInvocationResult.class, qn),
                               new BeanDeserializerFactory(RecipeInvocationResult.class, qn));                                 

      RecipeInvocation ri = new RecipeInvocation();
      ri.setRecipeName("test-recipe");

      RecipeInvocationResult ris = (RecipeInvocationResult) call.invoke(new Object[] { ri });

      System.out.println(ris.toString());
      
    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }
}
