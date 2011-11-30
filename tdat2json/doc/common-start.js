// run these commands immediately after importing the individual JSON files into the "catalog" collection

// add the empty xref array to all records. Use the _id field since an index exists by default
db.catalog.update({"_id":{$exists:true}}, {$set:{"xref":[]}}, false, true);

// need to add a space between HD and number. For some reason, hdname is already right
db.catalog.find({"name":/^HD[0-9]+/i}).forEach(function(obj) {
    obj.name = obj.hdname;
    db.catalog.save(obj);
});

// push the non-troublemaker values into the xref array
db.catalog.find().forEach(function(obj) {
    // star catalogs
    if (obj.bscname != null) {
        obj.xref.push(obj.bscname);
    }
    if (obj.cpdname != null) {
        obj.xref.push(obj.cpdname);
    }
    if (obj.gcname != null) {
        obj.xref.push(obj.gcname);
    }
    if (obj.gliesename != null) {
        obj.xref.push(obj.gliesename);
    }
    if (obj.hdname != null) {
        obj.xref.push(obj.hdname);
    }
    if (obj.hipname != null) {
        obj.xref.push(obj.hipname);
    }
    if (obj.ppmname != null) {
        obj.xref.push(obj.ppmname);
    }
    if (obj.saoname != null) {
        obj.xref.push(obj.saoname);
    }

    // galaxy catalogs
    if (obj.messiername != null) {
        obj.xref.push(obj.messiername);
    }
    if (obj.ngc2000name != null) {
        obj.xref.push(obj.ngc2000name);
    }
    if (obj.ugcname != null) {
        obj.xref.push(obj.ugcname);
    }
    if (obj.codname != null) {
        obj.xref.push(obj.codname);
    }
    if (obj.ccdmname != null) {
        obj.xref.push(obj.ccdmname);
    }
    db.catalog.save(obj);
});

db.catalog.update({"bscname":{$exists:true}}, {$unset:{"bscname":1}}, false, true);
db.catalog.update({"hdname":{$exists:true}}, {$unset:{"hdname":1}}, false, true);
db.catalog.update({"saoname":{$exists:true}}, {$unset:{"saoname":1}}, false, true);
db.catalog.update({"ppmname":{$exists:true}}, {$unset:{"ppmname":1}}, false, true);
db.catalog.update({"hipname":{$exists:true}}, {$unset:{"hipname":1}}, false, true);
db.catalog.update({"gcname":{$exists:true}}, {$unset:{"gcname":1}}, false, true);
db.catalog.update({"gliesename":{$exists:true}}, {$unset:{"gliesename":1}}, false, true);
db.catalog.update({"cpdname":{$exists:true}}, {$unset:{"cpdname":1}}, false, true);
db.catalog.update({"messiername":{$exists:true}}, {$unset:{"messiername":1}}, false, true);
db.catalog.update({"ngc2000name":{$exists:true}}, {$unset:{"ngc2000name":1}}, false, true);
db.catalog.update({"ugcname":{$exists:true}}, {$unset:{"ugcname":1}}, false, true);
db.catalog.update({"codname":{$exists:true}}, {$unset:{"codname":1}}, false, true);
db.catalog.update({"ccdmname":{$exists:true}}, {$unset:{"ccdmname":1}}, false, true);

db.catalog.ensureIndex({xref:1});


function fixArray2() {
    var counter = 0;
    // I only want the xref for each field, I don't even want the id
    var cursor = db.catalog.find({}, {xref: true, _id: false});

    // I don't want to init this inside the loop, worried about memory leaks (probably baseless worry)
    var consolidatedArray = [];
    while (cursor.hasNext()) {
        var xref1 = cursor.next().xref;
        // first pass: create a consolidated array when the cross references match
        var limitedCursor1 = db.catalog.find({"name":{$in:xref1}});
        while (limitedCursor1.hasNext()) {
            var doc1 = limitedCursor1.next();
            consolidatedArray = consolidatedArray.concat(doc1.xref);
        }
        consolidatedArray = consolidatedArray.unique();
        // now that we have the consolidated array, reset the xref field of the object to it
        for (var i=0; i<consolidatedArray.length; i++) {
            db.catalog.update({name:consolidatedArray[i]},{$set:{xref: consolidatedArray}},false, true);
        }

        consolidatedArray.length = 0;

        counter++;
        if (counter % 10000 == 0) {
            print("Processed " + counter + " documents.");
        }
    }
}
// at this point, run the catalog-specific scripts, then run the common-finish.js script. Now would be a good time to
// do a mongoexport... just sayin...
db.catalog.update({"ir_flag":{$exists:true}}, {$unset:{"ir_flag":1}}, false, true);
db.catalog.update({"astrom_ref_dbl":{$exists:true}}, {$unset:{"astrom_ref_dbl":1}}, false, true);