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
Array.prototype.unique = function(b) {
    var a = [], i, l = this.length;
    for (i = 0; i < l; i++) {
        if (a.indexOf(this[i], 0, b) < 0) {
            a.push(this[i]);
        }
    }
    return a;
};

function fixArray() {
    // get 'em all
    var cursor = db.catalog.find({}, {xref:true, "_id":false});
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
    emit(this.xref, {lii:this.lii, bii:this.bii});
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

db.runCommand({
    mapreduce: "catalog",
    map: function() {
        emit(this.xref, {lii:this.lii, bii:this.bii});
    },
    reduce: function(key, values) {
        var result = {lii: 0.0, bii: 0.0};

        values.forEach(function(value) {
            result.lii += value.lii;
            result.bii += value.bii;
        });

        result.bii /= values.length;
        result.lii /= values.length;
    },
    out:"catalog2"
});