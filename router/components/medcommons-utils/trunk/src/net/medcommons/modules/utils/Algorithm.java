/*
 * $Id: Algorithm.java 2622 2008-05-27 05:49:47Z ssadedin $
 */

package net.medcommons.modules.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some simple functional programming constructs
 */
public class Algorithm {
	
	public interface Filter<T> {
		boolean $(T t);
	}	

	public static <T extends Number> Number sum(Iterable<T> items) {
		double total = 0.0;
		for(T n : items) {
			total += n.doubleValue();
		}
		return total;
	}
	
	public static <T> List<T> filter(Iterable<T> items, Filter<T> p) {
		final List<T> filtered = new ArrayList<T>();
		for(T t : items) {
			if(p.$(t))
				filtered.add(t);
		}
		return filtered;
	}
	
	public static <T,U> List<U> map(Iterable<T> list, Function<U,T> c) {
		ArrayList<U> u = new ArrayList<U>();
		for(T t : list) 
			u.add(c.$(t));
		return u;
	}
	
	public static <T,U> Map<T,U> index(Iterable<U> list, Function<T,U> c) {
	    Map<T, U> result = new HashMap<T,U>();
	    for(U u: list) {
	        result.put(c.$(u), u);
	    }
	    return result;
	}
	
	
  /**
   * Simple test class for demonstration only
   */
	public static class Item {
		public Double price;

		public Item(Double price) {
			this.price = price;
		}
	}

	public static Number test(List<Item> items) {
		Function<Number, Item> getPrice = new Function<Number,Item>() {
			public Number $(Item u) {
				return u.price;
			}
		};		
		
		return sum(map(filter(items, new Filter<Item>() {
			public boolean $(Item item) {
				return item.price > 100;
			}
		}),new Function<Number,Item>() {
			public Number $(Item u) {
				return u.price;
			}
		}));
	}
	
  /**
   * Simple test to demonstrate use
   */
	public static void main(String[] args) {
		Double n [] = new Double[] { 20.2,  30.6, 150.2, 250.6 };
		
		Item items [] = new Item[] { new Item(20.2), new Item(30.6), new Item(150.2), new Item(250.6) };
		
		System.out.println(test(Arrays.asList(items)));
	}
}

