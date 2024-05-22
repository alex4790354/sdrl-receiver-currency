package com.github.alex4790354.repository;

import com.github.alex4790354.general.dto.CurrencyDto;
import com.github.alex4790354.general.dto.CurrencyRateDto;
import com.github.alex4790354.general.dto.CurrencyRateDtoDb;
import com.github.alex4790354.general.dto.MetalRateDto;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.OffsetDateTimeTypeHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Mapper
@Repository
public interface ReceiverCbrRepository {

    @Insert("""
            <script>
                MERGE INTO cbr.currency
                USING (
                    VALUES
                    <foreach item='item' collection='currenciesList' open='' separator=',' close=''>
                        (
                            #{item.id},
                            #{item.frequency},
                            #{item.nameRus},
                            #{item.nameEng},
                            #{item.nominal},
                            #{item.parentCode}
                        )
                    </foreach>
                ) AS source (id, frequency, name_rus, name_eng, nominal, parent_code)
                ON cbr.currency.id = source.id
                WHEN MATCHED THEN
                    UPDATE SET
                        frequency = source.frequency,
                        name_rus = source.name_rus,
                        name_eng = source.name_eng,
                        nominal = source.nominal,
                        parent_code = source.parent_code,
                        update_time = NOW()
                WHEN NOT MATCHED THEN
                    INSERT (id, frequency, name_rus, name_eng, nominal, parent_code, creation_time, update_time)
                    VALUES (source.id, source.frequency, source.name_rus, source.name_eng, source.nominal, source.parent_code, NOW(), NOW());
            </script>
            """)
    void saveCurrenciesList(@Param("currenciesList") List<CurrencyDto> currenciesList);


    @Transactional
    default void saveRatesListWithHistory(List<CurrencyRateDto> ratesList) {
        for (CurrencyRateDto rate : ratesList) {
            CurrencyRateDtoDb currentRate = findCurrentRateById(rate.getId());
            if (currentRate == null) {
                insertNewRate(rate);
            }
            else if (rate.getEffectiveDate().isBefore(currentRate.getEffectiveDate())) {
                upsertRateHistory(rate);
            }
            else {
                archiveCurrentRate(currentRate);
                updateCurrentRate(rate);
            }
        }
    }


    @Select("SELECT id, effective_date, nominal, first_crncy, second_crncy, value, creation_time FROM cbr.currency_rate WHERE id = #{id}")
    @Result(column = "CREATION_TIME", typeHandler = OffsetDateTimeTypeHandler.class)
    CurrencyRateDtoDb findCurrentRateById(String id);


    @Insert("""
                INSERT INTO cbr.currency_rate (id, effective_date, nominal, first_crncy, second_crncy, value)
                VALUES (#{rate.id}, TO_DATE(#{rate.dateAsString}, 'YYYY-MM-DD'), #{rate.nominal}, #{rate.charCode}, 'RUB', #{rate.value})
             """)
    void insertNewRate(@Param("rate") CurrencyRateDto rate);


    @Update("""
                UPDATE cbr.currency_rate
                    SET effective_date = #{rate.effectiveDate},
                        value = #{rate.value},
                        nominal = #{rate.nominal},
                        first_crncy = #{rate.charCode},
                        update_time = NOW()
                WHERE id = #{rate.id}
            """)
    void updateCurrentRate(@Param("rate") CurrencyRateDto rate);


    @Insert("""
            <script>
                MERGE INTO cbr.currency_rate_history AS target
                USING (
                    VALUES (
                        #{rate.id},
                        TO_DATE(#{rate.dateAsString}, 'YYYY-MM-DD'),
                        #{rate.nominal},
                        #{rate.charCode},
                        'RUB',
                        #{rate.value},
                        NOW()
                    )
                ) AS source (id, effective_date, nominal, first_crncy, second_crncy, value, creation_time)
                ON target.id = source.id AND target.effective_date = source.effective_date
                WHEN MATCHED THEN
                    UPDATE SET
                        value = source.value,
                        archived_at = NOW()
                WHEN NOT MATCHED THEN
                    INSERT (id, effective_date, nominal, first_crncy, second_crncy, value, creation_time, archived_at)
                    VALUES (source.id, source.effective_date, source.nominal, source.first_crncy, source.second_crncy, source.value, source.creation_time, NOW());
            </script>
            """)
    void upsertRateHistory(@Param("rate") CurrencyRateDto rate);


    @Insert("""
                <script>
                    MERGE INTO cbr.currency_rate_history AS target
                    USING (
                        VALUES (
                            #{rate.id},
                            #{rate.effectiveDate},
                            #{rate.nominal},
                            #{rate.firstCrncy},
                            #{rate.secondCrncy},
                            #{rate.value},
                            #{rate.creationTime},
                            NOW()
                        )
                    ) AS source (id, effective_date, nominal, first_crncy, second_crncy, value, creation_time, archived_at)
                    ON target.id = source.id AND target.effective_date = source.effective_date
                    WHEN MATCHED THEN
                        UPDATE SET
                            nominal = source.nominal,
                            first_crncy = source.first_crncy,
                            second_crncy = source.second_crncy,
                            value = source.value,
                            archived_at = NOW()
                    WHEN NOT MATCHED THEN
                        INSERT (id, effective_date, nominal, first_crncy, second_crncy, value, creation_time, archived_at)
                        VALUES (source.id, source.effective_date, source.nominal, source.first_crncy, source.second_crncy, source.value, source.creation_time, NOW());
                </script>
            """)
    void archiveCurrentRate(@Param("rate") CurrencyRateDtoDb rate);


    @Insert("""
             <script>
                 MERGE INTO cbr.metal_rate
                 USING (
                     VALUES
                     <foreach item='item' collection='metalRateDtoList' open='' separator=',' close=''>
                         (
                             #{item.code},
                             TO_DATE(#{item.dateAsString}, 'YYYY-MM-DD'),
                             #{item.buy},
                             #{item.sell},
                             #{item.value}
                         )
                     </foreach>
                 ) AS source (code, effective_date, buy, sell, value)
                 ON cbr.metal_rate.code = source.code AND cbr.metal_rate.effective_date = source.effective_date
                 WHEN MATCHED THEN
                     UPDATE SET
                         buy = source.buy,
                         sell = source.sell,
                         value = source.value,
                         update_time = NOW()
                 WHEN NOT MATCHED THEN
                     INSERT (code, effective_date, buy, sell, value, creation_time, update_time)
                     VALUES (source.code, source.effective_date, source.buy, source.sell, source.value, NOW(), NOW());
             </script>
            """)
    void saveMetalRates(@Param("metalRateDtoList") List<MetalRateDto> metalRateDtoList);


    // Alternative version to write scripts using 'ON CONFLICT' instead of MERGE
    @Insert("""
                <script>
                    INSERT INTO cbr.currency(id, frequency, name_rus, name_eng, nominal, parent_code)
                    VALUES
                        <foreach item='item' collection='currenciesList' open='' separator=',' close=''>
                            (
                                #{item.id},
                                #{item.frequency},
                                #{item.nameRus},
                                #{item.nameEng},
                                #{item.nominal},
                                #{item.parentCode}
                            )
                        </foreach>
                    ON CONFLICT (id)
                        DO
                            UPDATE SET
                                frequency=EXCLUDED.frequency,
                                name_rus=EXCLUDED.name_rus,
                                name_eng=EXCLUDED.name_eng,
                                nominal=EXCLUDED.nominal,
                                parent_code=EXCLUDED.parent_code,
                                update_time=NOW()
                </script>
            """)
    void saveCurrenciesListVersion2(@Param("currenciesList") List<CurrencyDto> currenciesList);

    @Insert("""
            INSERT INTO cbr.currency_rate_history (id, effective_date, nominal, first_crncy, second_crncy, value, creation_time, archived_at)
            VALUES (#{rate.id}, TO_DATE(#{rate.dateAsString}, 'YYYY-MM-DD'), #{rate.nominal}, #{rate.charCode}, 'RUB', #{rate.value}, now(), now())
            ON CONFLICT (id, effective_date)
                DO
                UPDATE SET
                    value = EXCLUDED.value,
                    archived_at = NOW()
            """)
    void upsertRateHistoryVersion2(@Param("rate") CurrencyRateDto rate);

    @Insert("""
                INSERT INTO cbr.currency_rate_history (id, effective_date, nominal, first_crncy, second_crncy, value, creation_time, archived_at)
                VALUES (#{rate.id}, #{rate.effectiveDate}, #{rate.nominal}, #{rate.firstCrncy}, #{rate.secondCrncy}, #{rate.value}, #{rate.creationTime}, NOW())
                ON CONFLICT (id, effective_date)
                    DO
                    UPDATE SET
                        nominal = EXCLUDED.nominal,
                        first_crncy = EXCLUDED.first_crncy,
                        second_crncy = EXCLUDED.second_crncy,
                        value = EXCLUDED.value,
                        archived_at = NOW()
            """)
    void archiveCurrentRateVersion2(@Param("rate") CurrencyRateDtoDb rate);

    @Insert("""
                <script>
                    INSERT INTO cbr.metal_rate(code, effective_date, buy, sell, value)
                    VALUES
                        <foreach item='item' collection='metalRateDtoList' open='' separator=',' close=''>
                            (
                                #{item.code},
                                TO_DATE(#{item.dateAsString}, 'YYYY-MM-DD'),
                                #{item.buy},
                                #{item.sell},
                                #{item.value}
                            )
                        </foreach>
                    ON CONFLICT (code, effective_date)
                        DO UPDATE SET
                                buy=EXCLUDED.buy,
                                sell=EXCLUDED.sell,
                                value=EXCLUDED.value,
                                update_time=NOW()
                </script>
            """)
    void saveMetalRatesVersion2(@Param("metalRateDtoList") List<MetalRateDto> metalRateDtoList);

}
