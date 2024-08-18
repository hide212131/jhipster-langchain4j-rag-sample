export interface IDocumentAsset {
  id?: number;
  filename?: string;
  dataContentType?: string;
  data?: string;
}

export const defaultValue: Readonly<IDocumentAsset> = {};
