db.catalog.group({
    key: {xref:true},
    cond: {xref:"HD 222237"},
    initial: {newXref:[]},
    reduce: function(obj, prev) {
        for (var i = 0; i < obj.xref.length; i++) {
            //print(obj.xref[i]);
            prev.newXref[prev.newXref.length] = obj.xref[i];
        }
    },
    finalize: function(obj) {
        print(obj);
        newXref = newXref.concat(obj.xref);
    }
});

db.catalog.find({xref: {"$in":"HD 222237"}});

var hd = db.catalog.findOne({xref:"HD 222237"}, {xref:true});

db.catalog.ensureIndex({"xref":1});

// Array.unique( strict ) - Remove duplicate values


function fixArray() {
    // get 'em all
    var cursor = db.cns3.find({}, {xref:true, "_id":false});
    var holdArray = [];
    while (cursor.hasNext()) {
        var doc = cursor.next();
        var docXref = doc.xref;
        var limitedCursor = db.catalog.find({"xref":{"$in":docXref}});
        while (limitedCursor.hasNext()) {
            var refDec = limitedCursor.next();
            holdArray = holdArray.concat(refDec.xref);
        }
        holdArray = Array.unique(holdArray);
        limitedCursor = db.catalog.find({"xref":{"$in":docXref}});
        while (limitedCursor.hasNext()) {
            var refDec = limitedCursor.next();
            refDec.xref = holdArray;
            db.catalog.save(refDec);
        }
        holdArray = [];
    }
}

function map() {
    emit({dummy:this.xref},
        {xref:this.xref,
            lii:this.lii,
            bii:this.bii,
            name:this.name,
            class:this.class,
            radeg:this.radeg,
            decdeg:this.decdeg,
            rapm: this.rapm,
            decpm:this.decpm,
            vmag: this.vmag,
            parallax:this.parallax});
}

function reduce(key, values) {
    var result = {xref:key, lii: 0.0, bii: 0.0};
    values.forEach(function(value) {
        if (value.lii && value.bii) {
            result.lii += value.lii;
            result.bii += value.bii;
        }
    });

    result.bii /= values.length;
    result.lii /= values.length;

    return result;
}

db.catalog2.find().forEach(function (obj) {
    var value = obj.value;
    db.getSisterDB("astro3").catalog3.save(value);
});

Array.prototype.unique = function(b) {
    var a = [], i, l = this.length;
    for (i = 0; i < l; i++) {
        if (a.indexOf(this[i], 0, b) < 0) {
            a.push(this[i]);
        }
    }
    return a;
};

Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};

Array.prototype.containsPattern = function(pattern) {
    // if we don't return a pattern, result is false
    if (pattern == null) {
        return false;
    }
    // if we have an empty or null array, return false
    if (this.length == 0 || this == null) {
        return false;
    }
    // if this isn't an array, return false
    if (!(this instanceof Array)) {
        return false;
    }
    //loop through the values to see if we get a match. checkMe is a boolean that is true if match returns
    // something, but by default
    var checkMe = false;

    for (var i = 0; i < this.length; i++) {
        var newString = new String(this[i]);
        if (newString.search(pattern) != -1) {
            checkMe = true;
            break;
        }
    }

    return checkMe;
}

db.catalog.find({name:/^PPM/i}).forEach(function (obj) {

    var xref = obj.xref;
    var pattern = /^-([0-9]+)\s([0-9]+)/i;
    for (var i = 0; i < xref.length; i++) {
        // try to only rename the BD/CD numbers that don't have a name attached
        if ((xref[i].indexOf("BD") == -1) && (xref[i].indexOf("CD") == -1)) {
            var matchArray = xref[i].toString().match(pattern);
            if (matchArray != null) {
                //print (xref[i]);

                var newName = "CD-" + matchArray[1] + " " + matchArray[2];
                print(newName);
                //obj.xref.push(newName);
            }
        }
    }
});

db.catalog.find({name:/^PPM/i}).forEach(function (obj) {
    var xref = obj.xref;
    if (xref.containsPattern("/^CPD/i")) {
        var pattern ="";
    }
    if (xref.containsPattern("/^BD/i")) {
        var pattern = "";
    }
});