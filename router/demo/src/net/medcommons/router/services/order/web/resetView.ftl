<html>
  <head>
    <script type='text/javascript'>
      if(parent.location.pathname.match(/\/view$/)) {
        location.href='viewEditCCR.do?ccrIndex=${ccrIndex}';
      }
      else {
        parent.location.href='view#' + '${view}';
      }
    </script>
  </head>
  <body onload='init();'>
  </body>
</html>
