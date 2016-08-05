function parse(body) {
  var google = {};
  google.visualization = {};
  google.visualization.Query = {};
  google.visualization.Query.setResponse = function (obj) {
    return obj;
  };
  var obj = eval(body);
  var cols = obj.table.cols;
  var rows = obj.table.rows;
  var res = {};
  for (var i = 1; i < cols.length; i++) {
    res[cols[i].label] = [];
  }
  var re = /^\d{2}(\d{2})年(\d{1,2})月/;
  for (var i = 0; i < rows.length - 1; i++) {
    var row = rows[i].c;
    var y0m = rows[i + 1].c[0].f.replace(re, '$10$2');
    var yymm = y0m.substr(0, 2) + y0m.substr(-2, 2);
    for (var j = 1; j < row.length; j++) {
      var rec = {};
      rec[yymm] = row[j].f;
      res[cols[j].label].push(rec);
    }
  }
  return res;
}
result = JSON.stringify(parse(body));
