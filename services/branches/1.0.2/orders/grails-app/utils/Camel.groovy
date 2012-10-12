class Camel {
  /**
   * Convert given map from underscore form to camelized form,
   * eg:   foo_bar  =>  fooBar
   */
  static Map toCamel(params) {
      return params.inject([:]) { m, entry ->
          def camel = entry.key.split('_').inject("") {  c,w -> 
            c+(c?w[0].toUpperCase():w[0]) + w[1..-1]
          }
          m[camel] = entry.value
          return m
      }
  }

  static String fromCamel(camel) {
    camel.toCharArray().inject(new StringBuilder()) { result, c ->
      if(c.isUpperCase())
         result << '_'
      
      result << c.toLowerCase()
    }
  }
}
