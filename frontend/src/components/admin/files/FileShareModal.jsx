import React from 'react';
import ShareModal from '../../ui/ShareModal';

const FileShareModal = ({ isOpen, onClose, file }) => {
    if (!file) {
        return null;
    }

    return (
        <ShareModal
            isOpen={isOpen}
            onClose={onClose}
            itemType="file"
            itemId={file.id}
            itemName={file.filename}
            getLinksUrl={`/admin/files/${file.id}/share`}
            createLinkUrl={`/admin/files/${file.id}/share`}
            deleteLinkUrlPrefix="/admin/files/share"
            publicUrlPrefix="/share"
        />
    );
};

export default FileShareModal;