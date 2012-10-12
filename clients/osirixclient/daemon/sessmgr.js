function test() {
    var elem = document.getElementById("foo");
    elem.firstChild.data = "Bar!ffffffffffffffffffffffffffffffffffBar!";
    var bar = document.getElementById("bar");
    bar.setAttribute("width", 16);
};

function format(status) {
    if ("action" in status && "tisteps" in status && "tnsteps" in status) {
	var elem = document.getElementById("action");
	elem.firstChild.data = status.action + "(Step " + status.tisteps + " of " + status.tnsteps + ")";
    }
    if ("status" in status) {
	var elem = document.getElementById("status");
	elem.firstChild.data = status.status;
    }
    if ("detail" in status) {
	var elem = document.getElementById("detail");
	elem.firstChild.data = status.detail;
    }
    var barimg = document.getElementById("barimg");
    var bardiv = document.getElementById("bardiv");
    var pctdiv = document.getElementById("pctdiv");
    if ("tibytes" in status && "tnbytes" in status) {
	barimg.setAttribute("width", (status.tibytes / status.tnbytes) * bardiv.getAttribute("width"));
	pctdiv.firstChild.data = percentFormat(status.tibytes / status.tnbytes) + " (" + status.tibytes + "/" + status.tnbytes + ")";
    }
    else {
	barimg.setAttribute("width", 0);
	pctdiv.firstChild.data = "";
    }
};

function enqueue() {
    var d = loadJSONDoc('http://localhost:16092/status?job=' + job);
    d.addCallBacks(success, failure);
};

function success(status) {
    format(status);
    enqueue();
};

function failure(status) {

};

