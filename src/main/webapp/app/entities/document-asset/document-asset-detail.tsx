import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, openFile, byteSize } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './document-asset.reducer';

export const DocumentAssetDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const documentAssetEntity = useAppSelector(state => state.documentAsset.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="documentAssetDetailsHeading">
          <Translate contentKey="jhipsterApp.documentAsset.detail.title">DocumentAsset</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{documentAssetEntity.id}</dd>
          <dt>
            <span id="filename">
              <Translate contentKey="jhipsterApp.documentAsset.filename">Filename</Translate>
            </span>
          </dt>
          <dd>{documentAssetEntity.filename}</dd>
          <dt>
            <span id="data">
              <Translate contentKey="jhipsterApp.documentAsset.data">Data</Translate>
            </span>
          </dt>
          <dd>
            {documentAssetEntity.data ? (
              <div>
                {documentAssetEntity.dataContentType ? (
                  <a onClick={openFile(documentAssetEntity.dataContentType, documentAssetEntity.data)}>
                    <Translate contentKey="entity.action.open">Open</Translate>&nbsp;
                  </a>
                ) : null}
                <span>
                  {documentAssetEntity.dataContentType}, {byteSize(documentAssetEntity.data)}
                </span>
              </div>
            ) : null}
          </dd>
        </dl>
        <Button tag={Link} to="/document-asset" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/document-asset/${documentAssetEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default DocumentAssetDetail;
