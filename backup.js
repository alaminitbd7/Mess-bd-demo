const fs = require('fs');
const path = require('path');
const admin = require('firebase-admin');

/**
 * Exports Firestore collections to a local JSON file for backup of mess financial records.
 * 
 * @param {Object} options Configuration options for the backup operation.
 * @param {string} options.serviceAccountPath Path to the Firebase service account credentials JSON.
 * @param {string} options.databaseURL (Optional) Firebase database URL.
 * @param {string[]} options.collections List of collection paths to export. Defaults to mess collections.
 * @param {string} options.outputPath Local file path where the JSON backup should be written.
 * @returns {Promise<void>} Resolves when export is complete.
 */
async function exportFirestoreBackup({
    serviceAccountPath,
    databaseURL = '',
    collections = ['members', 'payments', 'invoices', 'receipts', 'expenses'],
    outputPath = './mess_firestore_backup.json'
}) {
    if (!serviceAccountPath) {
        throw new Error('Missing serviceAccountPath parameter.');
    }

    // Initialize Firebase Admin SDK if not already initialized
    if (admin.apps.length === 0) {
        const serviceAccount = require(path.resolve(serviceAccountPath));
        const initConfig = {
            credential: admin.credential.cert(serviceAccount)
        };
        if (databaseURL) {
            initConfig.databaseURL = databaseURL;
        }
        admin.initializeApp(initConfig);
    }

    const db = admin.firestore();
    const backupData = {
        exportedAt: new Date().toISOString(),
        collections: {}
    };

    console.log(`Starting Firestore backup to: ${outputPath}`);

    for (const collectionName of collections) {
        try {
            console.log(`Exporting collection: "${collectionName}"...`);
            const snapshot = await db.collection(collectionName).get();
            const documents = [];

            snapshot.forEach(doc => {
                documents.push({
                    id: doc.id,
                    ...doc.data()
                });
            });

            backupData.collections[collectionName] = documents;
            console.log(`Exported ${documents.length} document(s) from "${collectionName}".`);
        } catch (error) {
            console.error(`Error exporting collection "${collectionName}":`, error.message);
            // We append the error state info to the JSON but do not stop the overall process
            backupData.collections[collectionName] = {
                error: error.message
            };
        }
    }

    try {
        const backupJson = JSON.stringify(backupData, null, 2);
        const absoluteOutputPath = path.resolve(outputPath);
        
        // Ensure parent directories exist
        const dir = path.dirname(absoluteOutputPath);
        if (!fs.existsSync(dir)) {
            fs.mkdirSync(dir, { recursive: true });
        }

        fs.writeFileSync(absoluteOutputPath, backupJson, 'utf-8');
        console.log(`Successfully completed Firestore backup! File saved at: ${absoluteOutputPath}`);
    } catch (writeError) {
        console.error('Failed to write backup JSON file:', writeError);
        throw writeError;
    }
}

module.exports = {
    exportFirestoreBackup
};
