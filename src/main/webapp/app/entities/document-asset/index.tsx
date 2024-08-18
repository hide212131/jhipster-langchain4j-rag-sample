import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import DocumentAsset from './document-asset';
import DocumentAssetDetail from './document-asset-detail';
import DocumentAssetUpdate from './document-asset-update';
import DocumentAssetDeleteDialog from './document-asset-delete-dialog';

const DocumentAssetRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<DocumentAsset />} />
    <Route path="new" element={<DocumentAssetUpdate />} />
    <Route path=":id">
      <Route index element={<DocumentAssetDetail />} />
      <Route path="edit" element={<DocumentAssetUpdate />} />
      <Route path="delete" element={<DocumentAssetDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DocumentAssetRoutes;
