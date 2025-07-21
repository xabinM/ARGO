 # 각자 모듈 완성 후 통합 작업
async def main():
    # Person A 결과물 사용
    collector = PublicAPICollector()
    raw_data = await collector.fetch_all_data()
    
    # Person B 결과물 사용  
    normalizer = DataNormalizer()
    normalized_data = normalizer.process(raw_data)
    
    # 기존 RAG 연동
    rag_pipeline = AdvancedRAGPipeline() 
    await rag_pipeline.initialize_with_data(normalized_data)
