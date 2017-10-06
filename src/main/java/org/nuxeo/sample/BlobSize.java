package org.nuxeo.sample;

import java.io.File;
import java.io.IOException;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
@Operation(id=BlobSize.ID, category=Constants.CAT_BLOB, label="BlobSize", description="Describe here what your operation does.")
public class BlobSize {

    public static final String ID = "Blob.BlobSize";
    private static final Log log = LogFactory.getLog(BlobSize.class);

    @Context
    protected CoreSession session;

    @Param(name = "writeToDisk", required = false, description = "if true, write blob to /tmp and compare sizes")
    protected boolean writeToDisk = false;

    @Param(name = "xpath", required = false, values = "file:content")
    protected String xpath = "file:content";

    protected File root;

    protected void init() {
        root = new File("/tmp");
        root.mkdirs();
    }

    protected File getFile(String name) {
        return new File(root, name);
    }

    protected void writeFile(Blob blob) throws IOException {
        String name = "blob#" + Integer.toHexString(System.identityHashCode(blob));
        // get the output file
        File file = getFile(name);
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        blob.transferTo(tmp);
        if (blob.getLength() == tmp.length()) {
        	log.debug("blob same size as file on disk");
        	log.debug(tmp.getAbsolutePath());
        }
        tmp.deleteOnExit(); // file on disk will be deleted when jvm exits (when nuxeo is stopped)
    }

    @OperationMethod
    public Blob run(DocumentModel doc) throws IOException {
		Blob blob = (Blob) doc.getPropertyValue(xpath);
		if (blob != null) {
			BlobHolder bh = doc.getAdapter(BlobHolder.class);
			if (bh != null) {
				blob = bh.getBlob();
                log.debug("blob length is " + blob.getLength());
                if ( writeToDisk ) {
                    init();
                    writeFile(blob);
                }
		    }
		} else { // blob is null
            // cannot return null since it may break the next operation
            // create an empty blob
            blob = Blobs.createBlob("");
            blob.setFilename(doc.getName() + ".null");
        }
        return blob;
    }
}

